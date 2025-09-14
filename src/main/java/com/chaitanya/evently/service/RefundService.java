package com.chaitanya.evently.service;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.exception.types.BadRequestException;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.chaitanya.evently.model.Refund;
import com.chaitanya.evently.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final RefundRepository refundRepository;

    @Transactional(readOnly = true)
    public Refund getRefundById(Long id) {
        Refund refund = refundRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Refund not found with id: " + id));
        return refund;
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Refund> getRefundsByUserId(Long userId, PaginationRequest paginationRequest,
            String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Refund> refundPage = refundRepository.findByUserId(userId, pageable);

        return PaginationResponse.fromPage(refundPage, baseUrl);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Refund> getAllRefunds(PaginationRequest paginationRequest, String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Refund> refundPage = refundRepository.findAll(pageable);

        return PaginationResponse.fromPage(refundPage, baseUrl);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Refund> getRefundsByBookingId(Long bookingId, PaginationRequest paginationRequest,
            String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        // Refunds for a booking are expected to be few; we can page over a list
        java.util.List<Refund> list = refundRepository.findByBookingId(bookingId);
        Page<Refund> refundPage = new org.springframework.data.domain.PageImpl<>(list, pageable, list.size());
        return PaginationResponse.fromPage(refundPage, baseUrl);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Refund> getRefundsByUserIdAndShowId(Long userId, Long showId,
            PaginationRequest paginationRequest, String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Refund> refundPage = refundRepository.findByUserIdAndShowId(userId, showId, pageable);

        return PaginationResponse.fromPage(refundPage, baseUrl);
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
