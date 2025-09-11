package com.chaitanya.evently.service.impl;

import com.chaitanya.evently.dto.seat.RowDefinition;
import com.chaitanya.evently.dto.seat.SectionDefinition;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.chaitanya.evently.model.Seat;
import com.chaitanya.evently.model.Venue;
import com.chaitanya.evently.repository.SeatRepository;
import com.chaitanya.evently.repository.VenueRepository;
import com.chaitanya.evently.service.SeatService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final VenueRepository venueRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Seat> listByVenue(Long venueId) {
        return seatRepository.findByVenueId(venueId);
    }

    @Override
    @Transactional
    public int createByMap(Long venueId, List<SectionDefinition> sections) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new NotFoundException("Venue with id=" + venueId + " not found"));

        List<Seat> toCreate = new ArrayList<>();
        for (SectionDefinition section : sections) {
            for (RowDefinition row : section.getRows()) {
                int start = row.getSeatNoStart();
                int count = row.getSeatCount();
                for (int i = 0; i < count; i++) {
                    Seat seat = new Seat();
                    seat.setVenue(venue);
                    seat.setSection(section.getSection());
                    seat.setRow(row.getRowNo());
                    seat.setSeatNumber(String.valueOf(start + i));
                    toCreate.add(seat);
                }
            }
        }
        seatRepository.saveAll(toCreate);
        return toCreate.size();
    }
}
