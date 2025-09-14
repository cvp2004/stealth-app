package com.chaitanya.evently.repository;

import com.chaitanya.evently.model.Booking;
import com.chaitanya.evently.model.status.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Updated query to fix parameter type determination issues

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId")
    Page<Booking> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.status = :status")
    List<Booking> findByStatus(@Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.show.id = :showId AND b.status = :status")
    List<Booking> findByShowIdAndStatus(@Param("showId") Long showId, @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.show.id = :showId")
    Page<Booking> findByUserIdAndShowId(@Param("userId") Long userId, @Param("showId") Long showId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.show.id = :showId")
    Page<Booking> findByShowId(@Param("showId") Long showId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.show.id IN :showIds")
    Page<Booking> findByShowIdIn(@Param("showIds") List<Long> showIds, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.show.id IN :showIds")
    Page<Booking> findByUserIdAndShowIdIn(@Param("userId") Long userId, @Param("showIds") List<Long> showIds,
            Pageable pageable);

    void deleteByShowId(Long showId);
}
