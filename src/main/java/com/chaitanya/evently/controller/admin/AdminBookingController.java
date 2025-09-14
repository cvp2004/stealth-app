package com.chaitanya.evently.controller.admin;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.model.Booking;
import com.chaitanya.evently.model.Event;
import com.chaitanya.evently.model.Venue;
import com.chaitanya.evently.model.User;
import com.chaitanya.evently.model.Show;
import com.chaitanya.evently.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/booking")
@RequiredArgsConstructor
@Slf4j
public class AdminBookingController {

    private final BookingService bookingService;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBookingById(@PathVariable Long id) {
        log.info("Admin requested booking with id: {}", id);
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(toBookingResponse(booking));
    }

    @GetMapping("/show/{showId}/list")
    public ResponseEntity<Map<String, Object>> getBookingsByShowId(
            @PathVariable Long showId,
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {

        log.info("Admin requested bookings for showId: {} - pagination: page={}, size={}",
                showId, paginationRequest.getPage(), paginationRequest.getSize());

        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<Booking> bookings = bookingService.getBookingsByShowId(showId, paginationRequest,
                baseUrl);
        return ResponseEntity.ok(toPaginationResponseMap(bookings));
    }

    @GetMapping("/event/{eventId}/list")
    public ResponseEntity<Map<String, Object>> getBookingsByEventId(
            @PathVariable Long eventId,
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {

        log.info("Admin requested bookings for eventId: {} - pagination: page={}, size={}",
                eventId, paginationRequest.getPage(), paginationRequest.getSize());

        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<Booking> bookings = bookingService.getBookingsByEventId(eventId, paginationRequest,
                baseUrl);
        return ResponseEntity.ok(toPaginationResponseMap(bookings));
    }

    @GetMapping("/venue/{venueId}/list")
    public ResponseEntity<Map<String, Object>> getBookingsByVenueId(
            @PathVariable Long venueId,
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {

        log.info("Admin requested bookings for venueId: {} - pagination: page={}, size={}",
                venueId, paginationRequest.getPage(), paginationRequest.getSize());

        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<Booking> bookings = bookingService.getBookingsByVenueId(venueId, paginationRequest,
                baseUrl);
        return ResponseEntity.ok(toPaginationResponseMap(bookings));
    }

    @GetMapping("/user/{userId}/list")
    public ResponseEntity<Map<String, Object>> getBookingsByUserId(
            @PathVariable Long userId,
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {

        log.info("Admin requested bookings for userId: {} - pagination: page={}, size={}",
                userId, paginationRequest.getPage(), paginationRequest.getSize());

        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<Booking> bookings = bookingService.getBookingsByUserId(userId, paginationRequest,
                baseUrl);
        return ResponseEntity.ok(toPaginationResponseMap(bookings));
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
                        "self", Optional.ofNullable(response.getLinks().getSelf()),
                        "first", Optional.ofNullable(response.getLinks().getFirst()),
                        "last", Optional.ofNullable(response.getLinks().getLast()),
                        "next", Optional.ofNullable(response.getLinks().getNext()),
                        "prev", Optional.ofNullable(response.getLinks().getPrev()));

        return Map.of(
                "isPaginated", response.isPaginated(),
                "content", content,
                "page", page,
                "sort", sort,
                "links", links);
    }
}
