package com.chaitanya.evently.controller.user;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.model.Payment;
import com.chaitanya.evently.service.PaymentService;
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
@RequestMapping("/api/v1/user/payment")
@RequiredArgsConstructor
@Slf4j
public class UserPaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPaymentById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = HeaderUtil.getUserIdFromHeader(request);
        log.info("User requested payment with id: {}, userId: {} (from header)", id, userId);
        Payment payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(toPaymentResponse(payment));
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getPayments(
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {
        Long userId = HeaderUtil.getUserIdFromHeader(request);
        log.info("User requested payments - userId: {} (from header), pagination: page={}, size={}",
                userId, paginationRequest.getPage(), paginationRequest.getSize());
        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<Payment> payments = paymentService.getPaymentsByUserId(userId, paginationRequest,
                baseUrl);
        return ResponseEntity.ok(toPaginationResponseMap(payments));
    }

    @GetMapping("/show/{showId}/list")
    public ResponseEntity<Map<String, Object>> getPaymentsByShowId(
            @PathVariable Long showId,
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {
        Long userId = HeaderUtil.getUserIdFromHeader(request);
        log.info("User {} requested payments for showId: {}, pagination: page={}, size={}",
                userId, showId, paginationRequest.getPage(), paginationRequest.getSize());
        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<Payment> payments = paymentService.getPaymentsByUserIdAndShowId(
                userId, showId, paginationRequest, baseUrl);
        return ResponseEntity.ok(toPaginationResponseMap(payments));
    }

    private Map<String, Object> toPaymentResponse(Payment payment) {
        return Map.of(
                "id", payment.getId(),
                "bookingId", payment.getBooking().getId(),
                "status", String.valueOf(payment.getStatus()),
                "amount", payment.getAmount(),
                "createdAt", payment.getCreatedAt());
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
