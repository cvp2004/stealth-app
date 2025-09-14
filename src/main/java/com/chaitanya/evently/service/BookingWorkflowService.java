package com.chaitanya.evently.service;

import com.chaitanya.evently.dto.seat.map.SeatMapResponse;
import com.chaitanya.evently.dto.booking.BookingCreateRequest;
import com.chaitanya.evently.dto.booking.BookingCreateResponse;
import com.chaitanya.evently.dto.booking.BookingPaymentRequest;
import com.chaitanya.evently.dto.booking.BookingPaymentResponse;
import com.chaitanya.evently.dto.booking.BookingCancelResponse;
import com.chaitanya.evently.dto.show.ShowSeatsResponse;
import com.chaitanya.evently.exception.types.BadRequestException;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.chaitanya.evently.model.Booking;
import com.chaitanya.evently.model.Email;
import com.chaitanya.evently.model.Payment;
import com.chaitanya.evently.model.Refund;
import com.chaitanya.evently.model.Seat;
import com.chaitanya.evently.model.Show;
import com.chaitanya.evently.model.Ticket;
import com.chaitanya.evently.model.User;
import com.chaitanya.evently.model.status.BookingStatus;
import com.chaitanya.evently.model.status.PaymentStatus;
import com.chaitanya.evently.model.status.RefundStatus;
import com.chaitanya.evently.repository.BookingRepository;
import com.chaitanya.evently.repository.EmailRepository;
import com.chaitanya.evently.repository.PaymentRepository;
import com.chaitanya.evently.repository.RefundRepository;
import com.chaitanya.evently.repository.SeatRepository;
import com.chaitanya.evently.repository.ShowRepository;
import com.chaitanya.evently.repository.TicketRepository;
import com.chaitanya.evently.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingWorkflowService {

    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final RefundRepository refundRepository;
    private final EmailRepository emailRepository;
    private final SimpleRedisService redisService;

    private static final int RESERVATION_TTL_SECONDS = 300; // 5 minutes

    /**
     * Gets show seats with availability information
     */
    public ShowSeatsResponse getShowSeats(Long showId) {
        Show show = findShowById(showId);

        List<Seat> allSeats = seatRepository.findByVenueId(show.getVenue().getId());
        List<Long> bookedSeatIds = ticketRepository.findBookedSeatIdsByShowId(showId);

        log.info("Retrieved {} total seats for show {}, {} already booked",
                allSeats.size(), showId, bookedSeatIds.size());

        return ShowSeatsResponse.builder()
                .seatMap(createSeatMapResponse(allSeats, bookedSeatIds))
                .bookedSeatIds(bookedSeatIds)
                .showId(showId)
                .showName("Show " + showId)
                .eventName(show.getEvent().getTitle())
                .venueName(show.getVenue().getName())
                .build();
    }

    /**
     * Creates a booking reservation and locks seats
     */
    public BookingCreateResponse createBooking(BookingCreateRequest request, Long userId) {
        log.info("Creating booking for user {} with {} seats for show {}",
                userId, request.getSeats().size(), request.getShowId());

        // Validate inputs
        validateBookingRequest(request);

        Show show = findShowById(request.getShowId());
        List<Seat> requestedSeats = findAndValidateSeats(request.getSeats(), show.getVenue().getId());
        List<Long> seatIds = requestedSeats.stream().map(Seat::getId).toList();

        // Check database for already booked seats
        validateSeatsNotBooked(seatIds, request.getShowId());

        // Check Redis for seat locks (if Redis is available)
        if (redisService.isRedisAvailable() && !redisService.areSeatsAvailable(seatIds)) {
            throw new BadRequestException("Some seats are currently being reserved by another user. Please try again.");
        }

        // Calculate total amount
        BigDecimal totalAmount = calculateTotalAmount(requestedSeats);

        // Create reservation with seat locking
        String reservationId = redisService.createReservation(
                userId,
                request.getShowId(),
                seatIds,
                totalAmount.toString());

        if (reservationId == null) {
            throw new BadRequestException(
                    "Selected seats are currently being reserved by another user. Please select different seats.");
        }

        log.info("Successfully created reservation {} for user {} with {} seats, total amount: {}",
                reservationId, userId, seatIds.size(), totalAmount);

        return BookingCreateResponse.builder()
                .reservationId(reservationId)
                .totalAmount(totalAmount)
                .expiresAt(Instant.now().plusSeconds(RESERVATION_TTL_SECONDS))
                .message("Seats reserved successfully. Complete payment within 5 minutes to confirm your booking.")
                .success(true)
                .build();
    }

    /**
     * Processes payment for a reservation and creates booking
     */
    @Transactional
    public BookingPaymentResponse processPayment(BookingPaymentRequest request, Long userId) {
        log.info("Processing payment for reservation {} by user {}", request.getReservationId(), userId);

        // Validate reservation
        SimpleRedisService.ReservationData reservation = validateReservation(request.getReservationId(), userId);

        // Validate payment amount
        validatePaymentAmount(request.getAmount(), reservation.getTotalAmount());

        try {
            // Create database entities atomically
            DatabaseEntities entities = createDatabaseEntities(reservation, userId, request.getAmount());

            // Prepare Redis transaction data
            SimpleRedisService.BookingTransactionData bookingData = createBookingTransactionData(entities, reservation);

            // Process Redis transaction (cleanup reservation and release locks)
            boolean transactionSuccess = redisService.processBookingTransaction(request.getReservationId(),
                    bookingData);

            if (!transactionSuccess) {
                log.error("Redis transaction failed for booking {}, but database changes are committed",
                        entities.booking.getId());
                // Note: In production, you might want to implement compensation logic here
                // For now, we'll continue as the database transaction succeeded
            }

            log.info("Successfully processed payment for booking {} with {} tickets, total amount: {}",
                    entities.booking.getId(), entities.tickets.size(), entities.booking.getTotalAmount());

            // Create booking confirmation email
            createBookingConfirmationEmail(entities.booking, entities.tickets);

            return BookingPaymentResponse.builder()
                    .bookingId(entities.booking.getId())
                    .message("Payment successful! Your booking has been confirmed. A confirmation email has been sent.")
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Error processing payment for reservation {}: {}", request.getReservationId(), e.getMessage());

            // Release reservation on failure
            redisService.releaseReservation(request.getReservationId());

            if (e instanceof BadRequestException || e instanceof NotFoundException) {
                throw e;
            }
            throw new BadRequestException("Payment processing failed: " + e.getMessage());
        }
    }

    /**
     * Cancels a booking and processes refund
     */
    @Transactional
    public BookingCancelResponse cancelBooking(Long bookingId, Long userId) {
        log.info("Cancelling booking {} for user {}", bookingId, userId);

        // Find and validate booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

        // Verify ownership
        if (!booking.getUser().getId().equals(userId)) {
            throw new BadRequestException("You are not authorized to cancel this booking");
        }

        // Check if already cancelled
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        // Check if booking can be cancelled (e.g., not too close to show time)
        validateBookingCancellation(booking);

        try {
            // Get tickets and payment information
            List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
            Payment payment = paymentRepository.findByBookingId(bookingId);

            if (payment == null) {
                throw new BadRequestException("No payment record found for this booking");
            }

            // Delete tickets (this frees up seats)
            ticketRepository.deleteAll(tickets);
            log.info("Deleted {} tickets for booking {}", tickets.size(), bookingId);

            // Create refund record
            Refund refund = createRefund(booking, payment);

            // Update booking status
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            log.info("Updated booking {} status to CANCELLED", bookingId);

            // Create cancellation email
            createCancellationEmail(booking, tickets, refund);

            log.info("Successfully cancelled booking {} with refund amount: {}", bookingId, refund.getAmount());

            return BookingCancelResponse.builder()
                    .bookingId(bookingId)
                    .message("Booking cancelled successfully. Refund will be processed within 3-5 business days.")
                    .success(true)
                    .refundAmount(refund.getAmount())
                    .build();

        } catch (Exception e) {
            log.error("Error cancelling booking {}: {}", bookingId, e.getMessage(), e);
            if (e instanceof BadRequestException || e instanceof NotFoundException) {
                throw e;
            }
            throw new BadRequestException("Failed to cancel booking: " + e.getMessage());
        }
    }

    // =============== PRIVATE HELPER METHODS ===============

    /**
     * Finds show by ID with validation
     */
    private Show findShowById(Long showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found with id: " + showId));
    }

    /**
     * Validates booking creation request
     */
    private void validateBookingRequest(BookingCreateRequest request) {
        if (request.getSeats() == null || request.getSeats().isEmpty()) {
            throw new BadRequestException("At least one seat must be selected");
        }
        if (request.getSeats().size() > 10) {
            throw new BadRequestException("Cannot book more than 10 seats at once");
        }
        if (request.getShowId() == null) {
            throw new BadRequestException("Show ID is required");
        }
    }

    /**
     * Finds and validates requested seats
     */
    private List<Seat> findAndValidateSeats(List<BookingCreateRequest.SeatRequest> seatRequests, Long venueId) {
        return seatRequests.stream()
                .map(seatRequest -> {
                    Seat seat = seatRepository.findByVenueIdAndSectionAndRowAndSeatNumber(
                            venueId,
                            seatRequest.getSection(),
                            seatRequest.getRow(),
                            seatRequest.getSeatNumber())
                            .orElseThrow(() -> new BadRequestException(
                                    String.format("Seat not found: %s-%s-%s",
                                            seatRequest.getSection(),
                                            seatRequest.getRow(),
                                            seatRequest.getSeatNumber())));

                    log.debug("Found seat: {} in section {}, row {}, seat number {}",
                            seat.getId(), seat.getSection(), seat.getRow(), seat.getSeatNumber());
                    return seat;
                })
                .toList();
    }

    /**
     * Validates that seats are not already booked in database
     */
    private void validateSeatsNotBooked(List<Long> seatIds, Long showId) {
        List<Long> bookedSeatIds = ticketRepository.findBookedSeatIdsByShowId(showId);
        List<Long> conflictingSeats = seatIds.stream()
                .filter(bookedSeatIds::contains)
                .toList();

        if (!conflictingSeats.isEmpty()) {
            throw new BadRequestException("Some seats are already booked: " + conflictingSeats);
        }
    }

    /**
     * Calculates total amount for seats
     */
    private BigDecimal calculateTotalAmount(List<Seat> seats) {
        return seats.stream()
                .map(seat -> BigDecimal.valueOf(100L))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Validates reservation exists and belongs to user
     */
    private SimpleRedisService.ReservationData validateReservation(String reservationId, Long userId) {
        SimpleRedisService.ReservationData reservation = redisService.getReservation(reservationId);

        if (reservation == null) {
            throw new BadRequestException("Reservation expired or not found. Please try booking again.");
        }

        if (!reservation.getUserId().equals(userId)) {
            throw new BadRequestException("You are not authorized to pay for this reservation.");
        }

        log.debug("Validated reservation {} for user {} with {} seats",
                reservationId, userId, reservation.getSeatIds().size());
        return reservation;
    }

    /**
     * Validates payment amount matches reservation amount
     */
    private void validatePaymentAmount(BigDecimal paymentAmount, String expectedAmountStr) {
        BigDecimal expectedAmount = new BigDecimal(expectedAmountStr);
        if (paymentAmount.compareTo(expectedAmount) != 0) {
            throw new BadRequestException(
                    String.format("Payment amount mismatch. Expected: %s, Received: %s",
                            expectedAmount, paymentAmount));
        }
    }

    /**
     * Validates if booking can be cancelled
     */
    private void validateBookingCancellation(Booking booking) {
        // Example: Don't allow cancellation within 24 hours of show
        Instant showTime = booking.getShow().getStartTimestamp();
        Instant now = Instant.now();

        if (showTime.isBefore(now.plusSeconds(86400))) { // 24 hours = 86400 seconds
            throw new BadRequestException("Cannot cancel booking within 24 hours of the show");
        }

        // Check if show has already started
        if (showTime.isBefore(now)) {
            throw new BadRequestException("Cannot cancel booking for a show that has already started");
        }
    }

    /**
     * Creates all database entities for a booking atomically
     */
    private DatabaseEntities createDatabaseEntities(SimpleRedisService.ReservationData reservation,
            Long userId, BigDecimal amount) {

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Get show
        Show show = findShowById(reservation.getShowId());

        // Get seats
        List<Seat> seats = seatRepository.findAllById(reservation.getSeatIds());
        if (seats.size() != reservation.getSeatIds().size()) {
            throw new BadRequestException("Some seats no longer exist");
        }

        // Create booking
        Booking booking = Booking.builder()
                .user(user)
                .show(show)
                .totalAmount(amount)
                .status(BookingStatus.CONFIRMED)
                .build();
        Booking savedBooking = bookingRepository.save(booking);

        // Create payment
        Payment payment = Payment.builder()
                .booking(savedBooking)
                .amount(amount)
                .status(PaymentStatus.SUCCESS)
                .build();
        Payment savedPayment = paymentRepository.save(payment);

        // Create tickets
        List<Ticket> tickets = seats.stream()
                .map(seat -> Ticket.builder()
                        .booking(savedBooking)
                        .seat(seat)
                        .price(BigDecimal.valueOf(100L))
                        .build())
                .toList();
        List<Ticket> savedTickets = ticketRepository.saveAll(tickets);

        log.info("Created booking {} with payment {} and {} tickets",
                savedBooking.getId(), savedPayment.getId(), savedTickets.size());

        return new DatabaseEntities(savedBooking, savedPayment, savedTickets, seats);
    }

    /**
     * Creates Redis transaction data for booking
     */
    private SimpleRedisService.BookingTransactionData createBookingTransactionData(
            DatabaseEntities entities, SimpleRedisService.ReservationData reservation) {

        Map<Long, String> seatPrices = entities.seats.stream()
                .collect(Collectors.toMap(
                        Seat::getId,
                        seat -> "100"));

        SimpleRedisService.PaymentData paymentData = SimpleRedisService.PaymentData.builder()
                .paymentId(entities.payment.getId())
                .bookingId(entities.booking.getId())
                .amount(entities.booking.getTotalAmount().toString())
                .status(PaymentStatus.SUCCESS.toString())
                .paymentMethod("ONLINE")
                .build();

        return SimpleRedisService.BookingTransactionData.builder()
                .bookingId(entities.booking.getId())
                .paymentId(entities.payment.getId())
                .userId(reservation.getUserId())
                .showId(reservation.getShowId())
                .totalAmount(entities.booking.getTotalAmount().toString())
                .seatPrices(seatPrices)
                .paymentData(paymentData)
                .build();
    }

    /**
     * Creates refund record
     */
    private Refund createRefund(Booking booking, Payment payment) {
        Refund refund = Refund.builder()
                .booking(booking)
                .payment(payment)
                .amount(booking.getTotalAmount())
                .status(RefundStatus.PENDING)
                .build();

        Refund savedRefund = refundRepository.save(refund);
        log.info("Created refund {} for booking {} with amount {}",
                savedRefund.getId(), booking.getId(), savedRefund.getAmount());

        return savedRefund;
    }

    /**
     * Creates seat map response from seats and booked seat IDs
     */
    private SeatMapResponse createSeatMapResponse(List<Seat> seats, List<Long> bookedSeatIds) {
        if (seats.isEmpty()) {
            return SeatMapResponse.builder()
                    .venueName("")
                    .totalCapacity(0)
                    .sections(List.of())
                    .build();
        }

        // Group seats by section and row
        Map<String, Map<String, List<Seat>>> groupedSeats = seats.stream()
                .collect(Collectors.groupingBy(Seat::getSection,
                        Collectors.groupingBy(Seat::getRow)));

        List<SeatMapResponse.Section> sections = groupedSeats.entrySet().stream()
                .map(sectionEntry -> {
                    String sectionId = sectionEntry.getKey();
                    Map<String, List<Seat>> rows = sectionEntry.getValue();

                    List<SeatMapResponse.Row> rowList = rows.entrySet().stream()
                            .map(rowEntry -> {
                                String rowId = rowEntry.getKey();
                                List<Seat> rowSeats = rowEntry.getValue();

                                List<Seat> sortedSeats = rowSeats.stream()
                                        .sorted((s1, s2) -> s1.getSeatNumber().compareTo(s2.getSeatNumber()))
                                        .toList();

                                return SeatMapResponse.Row.builder()
                                        .rowId(rowId)
                                        .seats(sortedSeats)
                                        .build();
                            })
                            .sorted((r1, r2) -> r1.getRowId().compareTo(r2.getRowId()))
                            .toList();

                    return SeatMapResponse.Section.builder()
                            .sectionId(sectionId)
                            .rows(rowList)
                            .build();
                })
                .sorted((s1, s2) -> s1.getSectionId().compareTo(s2.getSectionId()))
                .toList();

        return SeatMapResponse.builder()
                .venueName(seats.get(0).getVenue().getName())
                .totalCapacity(seats.size())
                .sections(sections)
                .build();
    }

    /**
     * Creates booking confirmation email
     */
    private void createBookingConfirmationEmail(Booking booking, List<Ticket> tickets) {
        String emailSubject = String.format("Booking Confirmation - %s (Booking #%d)",
                booking.getShow().getEvent().getTitle(), booking.getId());
        String emailBody = createBookingConfirmationEmailBody(booking, tickets);

        Email email = Email.builder()
                .user(booking.getUser())
                .emailType(Email.EmailType.BOOKING_CONFIRMATION)
                .emailSubject(emailSubject)
                .emailBody(emailBody)
                .build();

        emailRepository.save(email);
        log.info("Created booking confirmation email for user {} (booking {})",
                booking.getUser().getEmail(), booking.getId());
    }

    /**
     * Creates cancellation email
     */
    private void createCancellationEmail(Booking booking, List<Ticket> tickets, Refund refund) {
        String emailSubject = String.format("Booking Cancellation - %s (Booking #%d)",
                booking.getShow().getEvent().getTitle(), booking.getId());
        String emailBody = createCancellationEmailBody(booking, tickets, refund);

        Email email = Email.builder()
                .user(booking.getUser())
                .emailType(Email.EmailType.CANCEL_BOOKING)
                .emailSubject(emailSubject)
                .emailBody(emailBody)
                .build();

        emailRepository.save(email);
        log.info("Created cancellation email for user {} (booking {})",
                booking.getUser().getEmail(), booking.getId());
    }

    /**
     * Creates booking confirmation email body
     */
    private String createBookingConfirmationEmailBody(Booking booking, List<Ticket> tickets) {
        StringBuilder emailBody = new StringBuilder();

        emailBody.append("Dear ").append(booking.getUser().getFullName()).append(",\n\n");
        emailBody.append("🎉 Great news! Your booking has been successfully confirmed!\n\n");

        emailBody.append("═══════════════════════════════════\n");
        emailBody.append("           BOOKING DETAILS\n");
        emailBody.append("═══════════════════════════════════\n");
        emailBody.append("Booking ID: #").append(booking.getId()).append("\n");
        emailBody.append("Event: ").append(booking.getShow().getEvent().getTitle()).append("\n");
        emailBody.append("Date & Time: ").append(booking.getShow().getStartTimestamp().toString()).append("\n");
        emailBody.append("Venue: ").append(booking.getShow().getVenue().getName()).append("\n");
        emailBody.append("Total Amount Paid: $").append(booking.getTotalAmount()).append("\n\n");

        emailBody.append("═══════════════════════════════════\n");
        emailBody.append("          YOUR TICKETS (").append(tickets.size()).append(")\n");
        emailBody.append("═══════════════════════════════════\n");

        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = tickets.get(i);
            emailBody.append(String.format("🎫 Ticket %d: Section %s, Row %s, Seat %s - $%s\n",
                    i + 1,
                    ticket.getSeat().getSection(),
                    ticket.getSeat().getRow(),
                    ticket.getSeat().getSeatNumber(),
                    ticket.getPrice()));
        }

        emailBody.append("\n═══════════════════════════════════\n");
        emailBody.append("        IMPORTANT REMINDERS\n");
        emailBody.append("═══════════════════════════════════\n");
        emailBody.append("✅ Arrive at least 30 minutes before showtime\n");
        emailBody.append("✅ Bring a valid photo ID for verification\n");
        emailBody.append("✅ Present this email or booking ID at entry\n");
        emailBody.append("✅ Tickets are non-transferable\n");
        emailBody.append("✅ No outside food or beverages allowed\n\n");

        emailBody.append("Need help? Contact our support team at support@evently.com\n");
        emailBody.append("or call us at 1-800-EVENTLY\n\n");

        emailBody.append("Thank you for choosing Evently! 🎭\n");
        emailBody.append("We can't wait to see you at the show!\n\n");
        emailBody.append("Best regards,\n");
        emailBody.append("The Evently Team");

        return emailBody.toString();
    }

    /**
     * Creates cancellation email body
     */
    private String createCancellationEmailBody(Booking booking, List<Ticket> tickets, Refund refund) {
        StringBuilder emailBody = new StringBuilder();

        emailBody.append("Dear ").append(booking.getUser().getFullName()).append(",\n\n");
        emailBody.append("We've successfully processed your booking cancellation.\n\n");

        emailBody.append("═══════════════════════════════════\n");
        emailBody.append("       CANCELLATION DETAILS\n");
        emailBody.append("═══════════════════════════════════\n");
        emailBody.append("Cancelled Booking ID: #").append(booking.getId()).append("\n");
        emailBody.append("Event: ").append(booking.getShow().getEvent().getTitle()).append("\n");
        emailBody.append("Original Show Date: ").append(booking.getShow().getStartTimestamp().toString()).append("\n");
        emailBody.append("Venue: ").append(booking.getShow().getVenue().getName()).append("\n");
        emailBody.append("Number of Tickets: ").append(tickets.size()).append("\n\n");

        emailBody.append("═══════════════════════════════════\n");
        emailBody.append("         REFUND INFORMATION\n");
        emailBody.append("═══════════════════════════════════\n");
        emailBody.append("Refund ID: #").append(refund.getId()).append("\n");
        emailBody.append("Refund Amount: $").append(refund.getAmount()).append("\n");
        emailBody.append("Status: ").append(refund.getStatus().toString()).append("\n");
        emailBody.append("Processing Time: 3-5 business days\n\n");

        emailBody.append("💰 Your refund will be credited back to your original payment method.\n");
        emailBody.append("You'll receive a separate notification once the refund is processed.\n\n");

        emailBody.append("═══════════════════════════════════\n");
        emailBody.append("        CANCELLED TICKETS\n");
        emailBody.append("═══════════════════════════════════\n");

        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = tickets.get(i);
            emailBody.append(String.format("🎫 Ticket %d: Section %s, Row %s, Seat %s - $%s\n",
                    i + 1,
                    ticket.getSeat().getSection(),
                    ticket.getSeat().getRow(),
                    ticket.getSeat().getSeatNumber(),
                    ticket.getPrice()));
        }

        emailBody.append("\nWe're sorry to see you go! If you have any questions about this\n");
        emailBody.append("cancellation or need assistance with future bookings, please don't\n");
        emailBody.append("hesitate to contact us.\n\n");

        emailBody.append("Contact Support:\n");
        emailBody.append("📧 Email: support@evently.com\n");
        emailBody.append("📞 Phone: 1-800-EVENTLY\n\n");

        emailBody.append("Thank you for using Evently!\n\n");
        emailBody.append("Best regards,\n");
        emailBody.append("The Evently Team");

        return emailBody.toString();
    }

    /**
     * Helper record for database entities
     */
    private record DatabaseEntities(
            Booking booking,
            Payment payment,
            List<Ticket> tickets,
            List<Seat> seats) {
    }
}