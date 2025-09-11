package com.chaitanya.evently.service;

import com.chaitanya.evently.dto.seat.map.SeatMapRequest;
import com.chaitanya.evently.dto.seat.map.SeatMapResponse;
import com.chaitanya.evently.dto.seat.SeatResponse;
import java.util.List;

public interface SeatService {
    List<SeatResponse> listByVenue(Long venueId);

    SeatMapResponse getSeatMap(Long venueId);

    int createByMap(Long venueId, SeatMapRequest request);
}
