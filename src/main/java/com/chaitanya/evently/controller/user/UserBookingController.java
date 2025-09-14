package com.chaitanya.evently.controller.user;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.booking.BookingCancelRequest;
import com.chaitanya.evently.dto.booking.BookingCancelResponse;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.model.Booking;
import com.chaitanya.evently.model.Event;
import com.chaitanya.evently.model.Show;
import com.chaitanya.evently.model.User;
import com.chaitanya.evently.model.Venue;
import com.chaitanya.evently.service.BookingService;
import com.chaitanya.evently.service.BookingWorkflowService;
import com.chaitanya.evently.util.HeaderUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/booking")
@RequiredArgsConstructor
@Slf4j
public class UserBookingController {

    private final BookingService bookingService;
    private final BookingWorkflowService bookingWorkflowService;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBookingById(@PathVariable Long id, HttpServletRequest request) {
        // Get userId from header for authentication
        Long userId = HeaderUtil.getUserIdFromHeader(request);

        log.info("User requested booking with id: {}, userId: {} (from header)", id, userId);
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(toBookingResponse(booking));
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getBookings(
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {

        // Get userId from header
        Long userId = HeaderUtil.getUserIdFromHeader(request);

        log.info("User requested bookings - userId: {} (from header), pagination: page={}, size={}",
                userId, paginationRequest.getPage(), paginationRequest.getSize());

        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<Booking> bookings = bookingService.getBookingsByUserId(userId, paginationRequest, baseUrl);
        return ResponseEntity.ok(toPaginationResponseMap(bookings));
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<BookingCancelResponse> cancelBooking(
            @Valid @RequestBody BookingCancelRequest request,
            HttpServletRequest httpRequest) {

        // Get userId from header using HeaderUtil
        Long userId = HeaderUtil.getUserIdFromHeader(httpRequest);

        log.info("User {} requesting to cancel booking {}", userId, request.getBookingId());

        BookingCancelResponse response = bookingWorkflowService.cancelBooking(request.getBookingId(), userId);

        return ResponseEntity.ok(response);
    }

    // Private Mapper Methods

    private Map<String, Object> toBookingResponse(Booking booking) {
        return Map.of(
                "id", booking.getId(),
                "user", toUserResponse(booking.getUser()),
                "show", toShowResponse(booking.getShow()),
                "status", String.valueOf(booking.getStatus()),
                "totalAmount", booking.getTotalAmount(),
                "createdAt", booking.getCreatedAt());
    }

    private Map<String, Object> toShowResponse(Show show) {
        return Map.of(
                "id", show.getId(),
                "venue", toVenueResponse(show.getVenue()),
                "event", toEventResponse(show.getEvent()),
                "startTimestamp", show.getStartTimestamp(),
                "durationMinutes", show.getDurationMinutes(),
                "status", String.valueOf(show.getStatus()));
    }

    private Map<String, Object> toEventResponse(Event event) {
        return Map.of(
                "id", event.getId(),
                "title", event.getTitle(),
                "description", event.getDescription(),
                "category", event.getCategory());
    }

    private Map<String, Object> toVenueResponse(Venue venue) {
        return Map.of(
                "id", venue.getId(),
                "name", venue.getName(),
                "address", venue.getAddress());
    }

    private Map<String, Object> toUserResponse(User user) {
        return Map.of(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail());
    }

    private Map<String, Object> toPaginationResponseMap(PaginationResponse<Booking> response) {
        List<Map<String, Object>> content = response.getContent() == null ? List.of()
                : response.getContent().stream()
                        .map(this::toBookingResponse)
                        .collect(Collectors.toList());

        Map<String, Object> page = response.getPage() == null ? Map.of()
                : Map.of(
                        "number", response.getPage().getNumber(),
                        "size", response.getPage().getSize(),
                        "totalElements", response.getPage().getTotalElements(),
                        "totalPages", response.getPage().getTotalPages());

        List<Map<String, Object>> sortFields = (response.getSort() == null || response.getSort().getFields() == null)
                ? List.of()
                : response.getSort().getFields().stream()
                        .map(f -> Map.<String, Object>of(
                                "property", f.getProperty(),
                                "direction", f.getDirection()))
                        .collect(Collectors.toList());

        Map<String, Object> sort = Map.of("fields", sortFields);

        Map<String, Object> links = response.getLinks() == null ? Map.of()
                : Map.of(
                        "self", response.getLinks().getSelf(),
                        "first", response.getLinks().getFirst(),
                        "last", response.getLinks().getLast(),
                        "next", response.getLinks().getNext(),
                        "prev", response.getLinks().getPrev());

        return Map.of(
                "isPaginated", response.isPaginated(),
                "content", content,
                "page", page,
                "sort", sort,
                "links", links);
    }

}
