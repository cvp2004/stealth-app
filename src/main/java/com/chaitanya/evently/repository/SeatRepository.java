package com.chaitanya.evently.repository;

import com.chaitanya.evently.model.Seat;
import com.chaitanya.evently.repository.projection.SeatFlatProjection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByVenue_Id(Long venueId);

    List<SeatFlatProjection> findByVenue_IdOrderBySectionAscRowAscSeatNumberAsc(Long venueId);
}
