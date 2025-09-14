package com.chaitanya.evently.controller.user;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.model.Booking;
import com.chaitanya.evently.model.Event;
import com.chaitanya.evently.model.Seat;
import com.chaitanya.evently.model.Show;
import com.chaitanya.evently.model.Ticket;
import com.chaitanya.evently.model.User;
import com.chaitanya.evently.model.Venue;
import com.chaitanya.evently.service.TicketService;
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
@RequestMapping("/api/v1/user/tickets")
@RequiredArgsConstructor
@Slf4j
public class UserTicketController {

    private final TicketService ticketService;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTicketById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = HeaderUtil.getUserIdFromHeader(request);
        log.info("User requested ticket with id: {}, userId: {} (from header)", id, userId);
        Ticket ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(toTicketResponse(ticket));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<Map<String, Object>> getTicketsByBookingId(
            @PathVariable Long bookingId,
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {
        Long userId = HeaderUtil.getUserIdFromHeader(request);
        log.info("User {} requested tickets for bookingId: {}, pagination: page={}, size={}",
                userId, bookingId, paginationRequest.getPage(), paginationRequest.getSize());
        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<Ticket> tickets = ticketService.getTicketsByBookingId(bookingId, paginationRequest, baseUrl);
        return ResponseEntity.ok(toPaginationResponseMap(tickets));
    }

    // Private Helper Methods

    private Map<String, Object> toTicketResponse(Ticket ticket) {
        return Map.of(
                "id", ticket.getId(),
                "booking", toBookingResponse(ticket.getBooking()),
                "seat", toSeatResponse(ticket.getSeat()),
                "price", ticket.getPrice());
    }

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
                "title", event.getTitle());
    }

    private Map<String, Object> toVenueResponse(Venue venue) {
        return Map.of(
                "id", venue.getId(),
                "name", venue.getName());
    }

    private Map<String, Object> toUserResponse(User user) {
        return Map.of(
                "id", user.getId(),
                "fullName", user.getFullName());
    }

    private Map<String, Object> toSeatResponse(Seat seat) {
        return Map.of(
                "id", seat.getId(),
                "seat_label", seat.getSection() + "-" + seat.getRow() + "-" + seat.getSeatNumber());
    }

    private Map<String, Object> toPaginationResponseMap(PaginationResponse<Ticket> response) {
        List<Map<String, Object>> content = response.getContent() == null ? List.of()
                : response.getContent().stream()
                        .map(this::toTicketResponse)
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
