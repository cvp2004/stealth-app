package com.chaitanya.evently.repository;

import com.chaitanya.evently.model.Event;
import com.chaitanya.evently.model.status.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.category = :category")
    Page<Event> findByCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.status = :status")
    Page<Event> findByStatus(@Param("status") EventStatus status, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.category = :category AND e.status = :status")
    Page<Event> findByCategoryAndStatus(@Param("category") String category, @Param("status") EventStatus status,
            Pageable pageable);

    // User-specific queries - only show LIVE and CLOSED events
    @Query("SELECT e FROM Event e WHERE e.status IN ('LIVE', 'CLOSED')")
    Page<Event> findLiveAndClosedEvents(Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.category = :category AND e.status IN ('LIVE', 'CLOSED')")
    Page<Event> findLiveAndClosedEventsByCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.title = :title AND e.status IN ('LIVE', 'CLOSED')")
    Optional<Event> findLiveAndClosedEventByTitle(@Param("title") String title);

    Optional<Event> findByTitle(String title);

    boolean existsByTitle(String title);
}
