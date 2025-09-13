package com.chaitanya.evently.repository;

import com.chaitanya.evently.model.Show;
import com.chaitanya.evently.model.status.ShowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("SELECT s FROM Show s WHERE s.venue.id = :venueId")
    Page<Show> findByVenueId(@Param("venueId") Long venueId, Pageable pageable);

    @Query("SELECT s FROM Show s WHERE s.event.id = :eventId")
    Page<Show> findByEventId(@Param("eventId") Long eventId, Pageable pageable);

    @Query("SELECT s FROM Show s WHERE s.venue.id = :venueId AND s.event.id = :eventId")
    Page<Show> findByVenueIdAndEventId(@Param("venueId") Long venueId, @Param("eventId") Long eventId,
            Pageable pageable);

    @Query("SELECT s FROM Show s WHERE DATE(s.startTimestamp) = DATE(:date)")
    Page<Show> findByDate(@Param("date") Instant date, Pageable pageable);

    @Query("SELECT s FROM Show s WHERE s.startTimestamp >= :fromDate AND s.startTimestamp <= :toDate")
    Page<Show> findByDateRange(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate, Pageable pageable);

    @Query("SELECT s FROM Show s WHERE s.venue.id = :venueId AND DATE(s.startTimestamp) = DATE(:date)")
    Page<Show> findByVenueIdAndDate(@Param("venueId") Long venueId, @Param("date") Instant date, Pageable pageable);

    @Query("SELECT s FROM Show s WHERE s.event.id = :eventId AND DATE(s.startTimestamp) = DATE(:date)")
    Page<Show> findByEventIdAndDate(@Param("eventId") Long eventId, @Param("date") Instant date, Pageable pageable);

    @Query("SELECT s FROM Show s WHERE s.venue.id = :venueId AND s.startTimestamp >= :fromDate AND s.startTimestamp <= :toDate")
    Page<Show> findByVenueIdAndDateRange(@Param("venueId") Long venueId, @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate, Pageable pageable);

    @Query("SELECT s FROM Show s WHERE s.event.id = :eventId AND s.startTimestamp >= :fromDate AND s.startTimestamp <= :toDate")
    Page<Show> findByEventIdAndDateRange(@Param("eventId") Long eventId, @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate, Pageable pageable);

    @Query("SELECT s FROM Show s WHERE s.venue.id = :venueId AND s.event.id = :eventId AND s.startTimestamp >= :fromDate AND s.startTimestamp <= :toDate")
    Page<Show> findByVenueIdAndEventIdAndDateRange(@Param("venueId") Long venueId, @Param("eventId") Long eventId,
            @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate, Pageable pageable);

    @Query("SELECT s FROM Show s WHERE s.status = :status")
    List<Show> findByStatus(@Param("status") ShowStatus status);

    @Query("SELECT s FROM Show s WHERE s.event.id = :eventId AND s.status = :status")
    List<Show> findByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") ShowStatus status);

    void deleteByVenueId(Long venueId);

    void deleteByEventId(Long eventId);
}
