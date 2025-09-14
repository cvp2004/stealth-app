package com.chaitanya.evently.controller.admin;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.model.Booking;
import com.chaitanya.evently.model.Event;
import com.chaitanya.evently.model.Payment;
import com.chaitanya.evently.model.Show;
import com.chaitanya.evently.model.User;
import com.chaitanya.evently.model.Venue;
import com.chaitanya.evently.service.PaymentService;
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
@RequestMapping("/api/v1/admin/payment")
@RequiredArgsConstructor
@Slf4j
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPaymentById(@PathVariable Long id) {
        log.info("Admin requested payment with id: {}", id);
        Payment payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(toPaymentResponse(payment));
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getPayments(
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {

        log.info("Admin requested payments - pagination: page={}, size={}",
                paginationRequest.getPage(), paginationRequest.getSize());

        paginationRequest.setDirection("desc");

        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<Payment> payments = paymentService.getAllPayments(paginationRequest, baseUrl);
        return ResponseEntity.ok(toPaginationResponseMap(payments));
    }

    @GetMapping("/booking/{bookingId}/list")
    public ResponseEntity<Map<String, Object>> getPaymentsByBookingId(
            @PathVariable Long bookingId,
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {

        log.info("Admin requested payments for bookingId: {} - pagination: page={}, size={}",
                bookingId, paginationRequest.getPage(), paginationRequest.getSize());

        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<Payment> payments = paymentService.getPaymentsByBookingId(bookingId, paginationRequest,
                baseUrl);
        return ResponseEntity.ok(toPaginationResponseMap(payments));
    }

    // Private Helper Methods

    private Map<String, Object> toPaymentResponse(Payment payment) {
        return Map.of(
                "id", payment.getId(),
                "booking", toBookingResponse(payment.getBooking()),
                "status", String.valueOf(payment.getStatus()),
                "amount", payment.getAmount(),
                "createdAt", payment.getCreatedAt());
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
                "startTimestamp", show.getStartTimestamp());
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
                "fullName", user.getFullName(),
                "email", user.getEmail());
    }

    private Map<String, Object> toPaginationResponseMap(PaginationResponse<Payment> response) {
        List<Map<String, Object>> content = response.getContent() == null ? List.of()
                : response.getContent().stream()
                        .map(this::toPaymentResponse)
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