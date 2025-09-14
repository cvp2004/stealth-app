package com.chaitanya.evently.service;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.show.ShowRequest;
import com.chaitanya.evently.dto.show.ShowStatusUpdateRequest;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.exception.types.BadRequestException;
import com.chaitanya.evently.exception.types.ConflictException;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.chaitanya.evently.model.Booking;
import com.chaitanya.evently.model.Email;
import com.chaitanya.evently.model.Event;
import com.chaitanya.evently.model.Show;
import com.chaitanya.evently.model.Venue;
import com.chaitanya.evently.model.status.BookingStatus;
import com.chaitanya.evently.model.status.ShowStatus;
import com.chaitanya.evently.repository.BookingRepository;
import com.chaitanya.evently.repository.EmailRepository;
import com.chaitanya.evently.repository.EventRepository;
import com.chaitanya.evently.repository.ShowRepository;
import com.chaitanya.evently.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowService {

    private final ShowRepository showRepository;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final EmailRepository emailRepository;
    private final BookingWorkflowService bookingWorkflowService;

    @Transactional(readOnly = true)
    public Show getShowById(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Show not found with id: " + id));
        return show;
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Show> getAllShows(PaginationRequest paginationRequest, String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Show> showPage = showRepository.findAll(pageable);

        return PaginationResponse.fromPage(showPage, baseUrl);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Show> getShowsByVenueId(Long venueId, PaginationRequest paginationRequest,
            String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Show> showPage = showRepository.findByVenueId(venueId, pageable);

        return PaginationResponse.fromPage(showPage, baseUrl);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Show> getShowsByEventId(Long eventId, PaginationRequest paginationRequest,
            String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Show> showPage = showRepository.findByEventId(eventId, pageable);

        return PaginationResponse.fromPage(showPage, baseUrl);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Show> getShowsByVenueIdAndEventId(Long venueId, Long eventId,
            PaginationRequest paginationRequest, String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Show> showPage = showRepository.findByVenueIdAndEventId(venueId, eventId, pageable);

        return PaginationResponse.fromPage(showPage, baseUrl);
    }

    @Transactional
    public Show createShow(ShowRequest request) {
        // Validate venue exists
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new NotFoundException("Venue not found with id: " + request.getVenueId()));

        // Validate event exists
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + request.getEventId()));

        // Check for overlapping shows at the same venue
        if (hasOverlappingShows(request.getVenueId(), request.getStartTimestamp(), request.getDurationMinutes())) {
            throw new ConflictException("Show overlaps with existing show at the same venue");
        }

        Show show = new Show();
        show.setVenue(venue);
        show.setEvent(event);
        show.setStartTimestamp(request.getStartTimestamp());
        show.setDurationMinutes(request.getDurationMinutes());
        show.setStatus(ShowStatus.LIVE);

        Show savedShow = showRepository.save(show);
        log.info("Created show with id: {} for event: {} at venue: {}",
                savedShow.getId(), event.getTitle(), venue.getName());

        return savedShow;
    }

    @Transactional
    public Show updateShow(Long id, ShowRequest request) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Show not found with id: " + id));

        // Validate venue exists
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new NotFoundException("Venue not found with id: " + request.getVenueId()));

        // Validate event exists
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + request.getEventId()));

        // Check for overlapping shows at the same venue (excluding current show)
        if (hasOverlappingShowsExcludingCurrent(request.getVenueId(), request.getStartTimestamp(),
                request.getDurationMinutes(), id)) {
            throw new ConflictException("Show overlaps with existing show at the same venue");
        }

        show.setVenue(venue);
        show.setEvent(event);
        show.setStartTimestamp(request.getStartTimestamp());
        show.setDurationMinutes(request.getDurationMinutes());

        Show updatedShow = showRepository.save(show);
        log.info("Updated show with id: {} for event: {} at venue: {}",
                updatedShow.getId(), event.getTitle(), venue.getName());

        return updatedShow;
    }

    @Transactional
    public Show updateShowStatus(Long id, ShowStatusUpdateRequest request) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Show not found with id: " + id));

        ShowStatus currentStatus = show.getStatus();
        ShowStatus newStatus = request.getStatus();

        // Validate state transition
        validateShowStateTransition(currentStatus, newStatus);

        // Handle state transition logic
        if (currentStatus == ShowStatus.LIVE && newStatus == ShowStatus.CANCELLED) {
            // Cancel all bookings associated with the show
            log.info("Cancelling all bookings for show {} due to show cancellation", id);
            List<Booking> bookings = bookingRepository.findByShowId(id, Pageable.unpaged()).getContent();

            List<Booking> confirmedBookings = bookings.stream()
                    .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                    .toList();

            for (Booking booking : confirmedBookings) {
                bookingWorkflowService.cancelBooking(booking.getId(), booking.getUser().getId());

                String emailSubject = "Booking Cancellation Confirmation - Booking #" + booking.getId();
                String emailBody = createShowCancellationEmailBody(booking);

                Email email = Email.builder()
                        .user(booking.getUser())
                        .emailType(Email.EmailType.CANCEL_BOOKING)
                        .emailSubject(emailSubject)
                        .emailBody(emailBody)
                        .build();
                emailRepository.save(email);
                log.info("Created cancellation email for user {}", booking.getUser().getEmail());
            }
        }

        show.setStatus(newStatus);
        Show updatedShow = showRepository.save(show);
        log.info("Updated show status from {} to {} for show with id: {}",
                currentStatus, newStatus, updatedShow.getId());

        return updatedShow;
    }

    @Transactional
    public void deleteShow(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Show not found with id: " + id));

        showRepository.delete(show);
        log.info("Deleted show with id: {} for event: {} at venue: {}",
                id, show.getEvent().getTitle(), show.getVenue().getName());
    }

    private boolean hasOverlappingShows(Long venueId, Instant startTimestamp, Integer durationMinutes) {
        // Instant endTimestamp = startTimestamp.plusSeconds(durationMinutes * 60L);

        // // Check for overlapping shows at the same venue
        // List<Show> existingShows = showRepository.findByVenueId(venueId);

        // for (Show existingShow : existingShows) {
        // Instant existingStart = existingShow.getStartTimestamp();
        // Instant existingEnd =
        // existingStart.plusSeconds(existingShow.getDurationMinutes() * 60L);

        // // Check if shows overlap
        // if (startTimestamp.isBefore(existingEnd) &&
        // endTimestamp.isAfter(existingStart)) {
        // return true;
        // }
        // }

        return false;
    }

    private boolean hasOverlappingShowsExcludingCurrent(Long venueId, Instant startTimestamp, Integer durationMinutes,
            Long currentShowId) {
        Instant endTimestamp = startTimestamp.plusSeconds(durationMinutes * 60L);

        // Check for overlapping shows at the same venue (excluding current show)
        List<Show> existingShows = showRepository.findByVenueId(venueId);

        for (Show existingShow : existingShows) {
            if (existingShow.getId().equals(currentShowId)) {
                continue; // Skip current show
            }

            Instant existingStart = existingShow.getStartTimestamp();
            Instant existingEnd = existingStart.plusSeconds(existingShow.getDurationMinutes() * 60L);

            // Check if shows overlap
            if (startTimestamp.isBefore(existingEnd) && endTimestamp.isAfter(existingStart)) {
                return true;
            }
        }

        return false;
    }

    private void validateShowStateTransition(ShowStatus currentStatus, ShowStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // No change needed
        }

        switch (currentStatus) {
            case LIVE:
                if (newStatus != ShowStatus.CLOSED && newStatus != ShowStatus.CANCELLED) {
                    throw new BadRequestException("Show can only transition from LIVE to CLOSED or CANCELLED");
                }
                break;
            case CLOSED:
                throw new BadRequestException("Show is already CLOSED and cannot transition to any other status");
            case CANCELLED:
                throw new BadRequestException("Show is already CANCELLED and cannot transition to any other status");
            default:
                throw new BadRequestException("Invalid current status: " + currentStatus);
        }
    }

    private Pageable createPageable(PaginationRequest paginationRequest) {
        Sort.Direction direction = "desc".equalsIgnoreCase(paginationRequest.getDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // Only allow sorting by startTimestamp
        if (!"startTimestamp".equals(paginationRequest.getSort())) {
            throw new BadRequestException("Only 'startTimestamp' is allowed as sort field");
        }

        Sort sort = Sort.by(direction, "startTimestamp");
        return PageRequest.of(paginationRequest.getPage(), paginationRequest.getSize(), sort);
    }

    private String createShowCancellationEmailBody(Booking booking) {
        StringBuilder emailBody = new StringBuilder();

        emailBody.append("Dear ").append(booking.getUser().getFullName()).append(",\n\n");
        emailBody.append("We regret to inform you that the show has been cancelled.\n\n");

        emailBody.append("USER DETAILS:\n");
        emailBody.append("Name: ").append(booking.getUser().getFullName()).append("\n");
        emailBody.append("Email: ").append(booking.getUser().getEmail()).append("\n\n");

        emailBody.append("BOOKING DETAILS:\n");
        emailBody.append("Booking ID: ").append(booking.getId()).append("\n");
        emailBody.append("Event: ").append(booking.getShow().getEvent().getTitle()).append("\n");
        emailBody.append("Show Date: ").append(booking.getShow().getStartTimestamp()).append("\n");
        emailBody.append("Venue: ").append(booking.getShow().getVenue().getName()).append("\n");
        emailBody.append("Total Amount: $").append(booking.getTotalAmount()).append("\n\n");

        emailBody.append(
                "Due to the show cancellation, your booking has been automatically cancelled and a full refund will be processed within 3-5 business days.\n\n");
        emailBody.append(
                "We apologize for any inconvenience caused. If you have any questions, please contact our support team.\n\n");
        emailBody.append("Thank you for your understanding.\n\n");
        emailBody.append("The Evently Team");

        return emailBody.toString();
    }
}