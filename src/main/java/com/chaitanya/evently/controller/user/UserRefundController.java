package com.chaitanya.evently.controller.user;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.model.Refund;
import com.chaitanya.evently.service.RefundService;
import com.chaitanya.evently.util.HeaderUtil;
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
@RequestMapping("/api/v1/user/refund")
@RequiredArgsConstructor
@Slf4j
public class UserRefundController {

    private final RefundService refundService;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRefundById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = HeaderUtil.getUserIdFromHeader(request);
        log.info("User requested refund with id: {}, userId: {} (from header)", id, userId);
        Refund refund = refundService.getRefundById(id);
        return ResponseEntity.ok(toRefundResponse(refund));
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getRefunds(
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {
        Long userId = HeaderUtil.getUserIdFromHeader(request);
        log.info("User requested refunds - userId: {} (from header), pagination: page={}, size={}",
                userId, paginationRequest.getPage(), paginationRequest.getSize());
        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<Refund> refunds = refundService.getRefundsByUserId(userId, paginationRequest,
                baseUrl);
        return ResponseEntity.ok(toPaginationResponseMap(refunds));
    }

    @GetMapping("/show/{showId}/list")
    public ResponseEntity<Map<String, Object>> getRefundsByShowId(
            @PathVariable Long showId,
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {
        Long userId = HeaderUtil.getUserIdFromHeader(request);
        log.info("User {} requested refunds for showId: {}, pagination: page={}, size={}",
                userId, showId, paginationRequest.getPage(), paginationRequest.getSize());
        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<Refund> refunds = refundService.getRefundsByUserIdAndShowId(
                userId, showId, paginationRequest, baseUrl);
        return ResponseEntity.ok(toPaginationResponseMap(refunds));
    }

    private Map<String, Object> toRefundResponse(Refund refund) {
        return Map.of(
                "id", refund.getId(),
                "bookingId", refund.getBooking().getId(),
                "paymentId", refund.getPayment().getId(),
                "amount", refund.getAmount(),
                "createdAt", refund.getCreatedAt());
    }

    private Map<String, Object> toPaginationResponseMap(PaginationResponse<Refund> response) {
        List<Map<String, Object>> content = response.getContent() == null ? List.of()
                : response.getContent().stream()
                        .map(this::toRefundResponse)
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
