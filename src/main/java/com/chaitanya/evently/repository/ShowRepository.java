package com.chaitanya.evently.repository;

import com.chaitanya.evently.model.Show;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long>, JpaSpecificationExecutor<Show> {

    // Basic queries
    boolean existsByTitleAndVenueId(String title, Long venueId);

    List<Show> findByVenueId(Long venueId);

    List<Show> findByEventId(Long eventId);

    List<Show> findByVenueIdAndEventId(Long venueId, Long eventId);

    // Date-based queries
    List<Show> findByStartTimestampBetween(Instant start, Instant end);

    List<Show> findByStartTimestampAfter(Instant start);

    List<Show> findByStartTimestampBefore(Instant end);

    // Complex queries using @Query
    @Query("SELECT s FROM Show s WHERE " +
            "(:venueId IS NULL OR s.venue.id = :venueId) AND " +
            "(:eventId IS NULL OR s.event.id = :eventId) AND " +
            "(:date IS NULL OR DATE(s.startTimestamp) = DATE(:date)) AND " +
            "(:fromDate IS NULL OR s.startTimestamp >= :fromDate) AND " +
            "(:toDate IS NULL OR s.startTimestamp <= :toDate)")
    Page<Show> findShowsWithFilters(
            @Param("venueId") Long venueId,
            @Param("eventId") Long eventId,
            @Param("date") Instant date,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable);

    // Check for time conflicts
    @Query(value = "SELECT COUNT(s) > 0 FROM shows s WHERE " +
            "s.venue_id = :venueId AND " +
            "s.id != :excludeId AND " +
            "((s.start_timestamp < :endTime AND (s.start_timestamp + INTERVAL '1 minute' * s.duration_minutes) > :startTime))", nativeQuery = true)
    boolean hasTimeConflict(
            @Param("venueId") Long venueId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("excludeId") Long excludeId);
}
