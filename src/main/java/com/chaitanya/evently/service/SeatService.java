package com.chaitanya.evently.service;

import com.chaitanya.evently.dto.seat.SectionDefinition;
import com.chaitanya.evently.model.Seat;
import java.util.List;

public interface SeatService {
    List<Seat> listByVenue(Long venueId);

    int createByMap(Long venueId, List<SectionDefinition> sections);
}
