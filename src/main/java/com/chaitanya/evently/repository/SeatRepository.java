package com.chaitanya.evently.repository;

import com.chaitanya.evently.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByVenueId(Long venueId);

    @Query("SELECT s FROM Seat s WHERE s.venue.id = :venueId ORDER BY s.section, s.row, s.seatNumber")
    List<Seat> findByVenueIdOrdered(@Param("venueId") Long venueId);

    boolean existsByVenueIdAndSectionAndRowAndSeatNumber(Long venueId, String section, String row, String seatNumber);

    void deleteByVenueId(Long venueId);

    long countByVenueId(Long venueId);
}
