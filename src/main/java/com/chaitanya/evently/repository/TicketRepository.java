package com.chaitanya.evently.repository;

import com.chaitanya.evently.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t FROM Ticket t WHERE t.booking.user.id = :userId")
    Page<Ticket> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.booking.id = :bookingId")
    Long countByBookingId(@Param("bookingId") Long bookingId);

    @Query("SELECT t.seat.id FROM Ticket t WHERE t.booking.show.id = :showId")
    List<Long> findBookedSeatIdsByShowId(@Param("showId") Long showId);

    @Query("SELECT t FROM Ticket t WHERE t.booking.id = :bookingId")
    List<Ticket> findByBookingId(@Param("bookingId") Long bookingId);

    @Query("SELECT t FROM Ticket t WHERE t.booking.user.id = :userId AND t.booking.show.id = :showId")
    Page<Ticket> findByUserIdAndShowId(@Param("userId") Long userId, @Param("showId") Long showId, Pageable pageable);

    void deleteByBookingId(Long bookingId);
}
