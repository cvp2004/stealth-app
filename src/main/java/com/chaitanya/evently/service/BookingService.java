package com.chaitanya.evently.service;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.booking.BookingStatusUpdateRequest;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.exception.types.BadRequestException;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.chaitanya.evently.model.Booking;
import com.chaitanya.evently.model.status.BookingStatus;
import com.chaitanya.evently.repository.BookingRepository;
import com.chaitanya.evently.repository.ShowRepository;
import com.chaitanya.evently.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final ShowRepository showRepository;
    private final BookingWorkflowService bookingWorkflowService;

    @Transactional(readOnly = true)
    public Booking getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));
        return booking;
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Booking> getBookingsByUserId(Long userId, PaginationRequest paginationRequest,
            String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Booking> bookingPage = bookingRepository.findByUserId(userId, pageable);

        return PaginationResponse.fromPage(bookingPage, baseUrl);
    }

    @Transactional
    public Booking updateBookingStatus(Long id, BookingStatusUpdateRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));

        BookingStatus currentStatus = booking.getStatus();
        BookingStatus newStatus = request.getStatus();

        // Validate state transition
        validateBookingStateTransition(currentStatus, newStatus);

        booking.setStatus(newStatus);

        // If booking is cancelled, delete all associated tickets
        if (newStatus == BookingStatus.CANCELLED) {
            bookingWorkflowService.cancelBooking(id, booking.getUser().getId());
            log.info("Cancelled booking with id: {}", id);
        }

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Updated booking status from {} to {} for booking with id: {}",
                currentStatus, newStatus, updatedBooking.getId());

        return updatedBooking;
    }

    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));

        // Delete all associated tickets first
        ticketRepository.deleteByBookingId(id);

        bookingRepository.delete(booking);
        log.info("Deleted booking with id: {} and all associated tickets", id);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Booking> getBookingsByVenueId(Long venueId, PaginationRequest paginationRequest,
            String baseUrl) {
        // Get all shows for the venue, then get bookings for those shows
        List<Long> showIds = showRepository.findByVenueId(venueId).stream()
                .map(show -> show.getId())
                .toList();

        if (showIds.isEmpty()) {
            return PaginationResponse.<Booking>builder()
                    .isPaginated(false)
                    .content(java.util.Collections.emptyList())
                    .page(PaginationResponse.PageMeta.builder()
                            .number(0)
                            .size(paginationRequest.getSize())
                            .totalElements(0)
                            .totalPages(0)
                            .build())
                    .sort(PaginationResponse.SortMeta.builder()
                            .fields(java.util.Collections.emptyList())
                            .build())
                    .links(PaginationResponse.Links.builder()
                            .self(baseUrl)
                            .first(baseUrl)
                            .last(baseUrl)
                            .build())
                    .build();
        }

        Pageable pageable = createPageable(paginationRequest);
        Page<Booking> bookingPage = bookingRepository.findByShowIdIn(showIds, pageable);

        return PaginationResponse.fromPage(bookingPage, baseUrl);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Booking> getBookingsByShowId(Long showId, PaginationRequest paginationRequest,
            String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Booking> bookingPage = bookingRepository.findByShowId(showId, pageable);

        return PaginationResponse.fromPage(bookingPage, baseUrl);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Booking> getBookingsByEventId(Long eventId, PaginationRequest paginationRequest,
            String baseUrl) {
        // Get all shows for the event, then get bookings for those shows
        List<Long> showIds = showRepository.findByEventId(eventId).stream()
                .map(show -> show.getId())
                .toList();

        if (showIds.isEmpty()) {
            return PaginationResponse.<Booking>builder()
                    .isPaginated(false)
                    .content(java.util.Collections.emptyList())
                    .page(PaginationResponse.PageMeta.builder()
                            .number(0)
                            .size(paginationRequest.getSize())
                            .totalElements(0)
                            .totalPages(0)
                            .build())
                    .sort(PaginationResponse.SortMeta.builder()
                            .fields(java.util.Collections.emptyList())
                            .build())
                    .links(PaginationResponse.Links.builder()
                            .self(baseUrl)
                            .first(baseUrl)
                            .last(baseUrl)
                            .build())
                    .build();
        }

        Pageable pageable = createPageable(paginationRequest);
        Page<Booking> bookingPage = bookingRepository.findByShowIdIn(showIds, pageable);

        return PaginationResponse.fromPage(bookingPage, baseUrl);
    }

    private void validateBookingStateTransition(BookingStatus currentStatus, BookingStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // No change needed
        }

        switch (currentStatus) {
            case CONFIRMED:
                if (newStatus != BookingStatus.CANCELLED) {
                    throw new BadRequestException("Booking can only transition from CONFIRMED to CANCELLED");
                }
                break;
            case CANCELLED:
                throw new BadRequestException("Booking is already CANCELLED and cannot transition to any other status");
            default:
                throw new BadRequestException("Invalid current status: " + currentStatus);
        }
    }

    private Pageable createPageable(PaginationRequest paginationRequest) {
        Sort.Direction direction = "desc".equalsIgnoreCase(paginationRequest.getDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // Only allow sorting by createdAt
        if (!"createdAt".equals(paginationRequest.getSort())) {
            throw new BadRequestException("Only 'createdAt' is allowed as sort field");
        }

        Sort sort = Sort.by(direction, "createdAt");
        return PageRequest.of(paginationRequest.getPage(), paginationRequest.getSize(), sort);
    }

}
