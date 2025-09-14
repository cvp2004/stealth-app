package com.chaitanya.evently.repository;

import com.chaitanya.evently.model.Payment;
import com.chaitanya.evently.model.status.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p WHERE p.booking.user.id = :userId")
    Page<Payment> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    List<Payment> findByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId and p.status = 'SUCCESS'")
    Payment findByBookingId(@Param("bookingId") Long bookingId);

    @Query("SELECT p FROM Payment p WHERE p.booking.user.id = :userId AND p.booking.show.id = :showId")
    Page<Payment> findByUserIdAndShowId(@Param("userId") Long userId, @Param("showId") Long showId, Pageable pageable);

    void deleteByBookingId(Long bookingId);
}
