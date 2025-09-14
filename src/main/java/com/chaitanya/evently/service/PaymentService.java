package com.chaitanya.evently.service;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.payment.PaymentStatusUpdateRequest;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.exception.types.BadRequestException;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.chaitanya.evently.model.Payment;
import com.chaitanya.evently.model.status.PaymentStatus;
import com.chaitanya.evently.repository.PaymentRepository;
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
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found with id: " + id));
        return payment;
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Payment> getPaymentsByUserId(Long userId, PaginationRequest paginationRequest,
            String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Payment> paymentPage = paymentRepository.findByUserId(userId, pageable);

        return PaginationResponse.fromPage(paymentPage, baseUrl);
    }

    @Transactional
    public Payment updatePaymentStatus(Long id, PaymentStatusUpdateRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found with id: " + id));

        PaymentStatus currentStatus = payment.getStatus();
        PaymentStatus newStatus = request.getStatus();

        // Validate state transition
        validatePaymentStateTransition(currentStatus, newStatus);

        payment.setStatus(newStatus);
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Updated payment status from {} to {} for payment with id: {}",
                currentStatus, newStatus, updatedPayment.getId());

        return updatedPayment;
    }

    @Transactional
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found with id: " + id));

        paymentRepository.delete(payment);
        log.info("Deleted payment with id: {} for booking: {}", id, payment.getBooking().getId());
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Payment> getAllPayments(PaginationRequest paginationRequest, String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Payment> paymentPage = paymentRepository.findAll(pageable);

        return PaginationResponse.fromPage(paymentPage, baseUrl);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Payment> getPaymentsByBookingId(Long bookingId, PaginationRequest paginationRequest,
            String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        // findByBookingId returns a single Payment; wrap it in a one-item page if
        // present
        Payment payment = paymentRepository.findByBookingId(bookingId);
        java.util.List<Payment> list = payment == null ? java.util.List.of() : java.util.List.of(payment);
        Page<Payment> paymentPage = new org.springframework.data.domain.PageImpl<>(list, pageable, list.size());
        return PaginationResponse.fromPage(paymentPage, baseUrl);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Payment> getPaymentsByUserIdAndShowId(Long userId, Long showId,
            PaginationRequest paginationRequest, String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Payment> paymentPage = paymentRepository.findByUserIdAndShowId(userId, showId, pageable);

        return PaginationResponse.fromPage(paymentPage, baseUrl);
    }

    private void validatePaymentStateTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // No change needed
        }

        switch (currentStatus) {
            case SUCCESS:
                throw new BadRequestException("Payment is already SUCCESS and cannot transition to any other status");
            case FAILED:
                if (newStatus != PaymentStatus.SUCCESS) {
                    throw new BadRequestException("Failed payment can only transition to SUCCESS");
                }
                break;
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
