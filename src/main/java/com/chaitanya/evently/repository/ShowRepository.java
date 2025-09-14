package com.chaitanya.evently.repository;

import com.chaitanya.evently.model.Show;
import com.chaitanya.evently.model.status.ShowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("SELECT s FROM Show s WHERE s.status = :status")
    List<Show> findByStatus(@Param("status") ShowStatus status);

    @Query("SELECT s FROM Show s WHERE s.event.id = :eventId AND s.status = :status")
    List<Show> findByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") ShowStatus status);

    @Query("SELECT s FROM Show s WHERE s.venue.id = :venueId")
    List<Show> findByVenueId(@Param("venueId") Long venueId);

    @Query("SELECT s FROM Show s WHERE s.venue.id = :venueId")
    Page<Show> findByVenueId(@Param("venueId") Long venueId, Pageable pageable);

    @Query("SELECT s FROM Show s WHERE s.event.id = :eventId")
    Page<Show> findByEventId(@Param("eventId") Long eventId, Pageable pageable);

    @Query("SELECT s FROM Show s WHERE s.event.id = :eventId")
    List<Show> findByEventId(@Param("eventId") Long eventId);

    @Query("SELECT s FROM Show s WHERE s.venue.id = :venueId AND s.event.id = :eventId")
    Page<Show> findByVenueIdAndEventId(@Param("venueId") Long venueId, @Param("eventId") Long eventId,
            Pageable pageable);

    void deleteByVenueId(Long venueId);

    void deleteByEventId(Long eventId);
}