package com.chaitanya.evently.repository;

import com.chaitanya.evently.model.Refund;
import com.chaitanya.evently.model.status.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    @Query("SELECT r FROM Refund r WHERE r.booking.user.id = :userId")
    Page<Refund> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT r FROM Refund r WHERE r.status = :status")
    List<Refund> findByStatus(@Param("status") RefundStatus status);

    @Query("SELECT r FROM Refund r WHERE r.booking.user.id = :userId AND r.booking.show.id = :showId")
    Page<Refund> findByUserIdAndShowId(@Param("userId") Long userId, @Param("showId") Long showId, Pageable pageable);

    @Query("SELECT r FROM Refund r WHERE r.booking.id = :bookingId")
    List<Refund> findByBookingId(@Param("bookingId") Long bookingId);

    void deleteByBookingId(Long bookingId);
}
