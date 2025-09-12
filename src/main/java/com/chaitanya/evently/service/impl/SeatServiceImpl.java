package com.chaitanya.evently.service.impl;

import com.chaitanya.evently.dto.seat.SeatResponse;
import com.chaitanya.evently.dto.seat.map.SeatMapRequest;
import com.chaitanya.evently.dto.seat.map.SeatMapResponse;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.chaitanya.evently.model.Seat;
import com.chaitanya.evently.model.Venue;
import com.chaitanya.evently.repository.SeatRepository;
import com.chaitanya.evently.repository.VenueRepository;
import com.chaitanya.evently.service.SeatService;
import com.chaitanya.evently.util.SeatMapBuilder;
import java.util.List;
import java.util.stream.Collectors;
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
    public List<SeatResponse> listByVenue(Long venueId) {
        return seatRepository.findByVenue_Id(venueId).stream()
                .map(s -> SeatResponse.builder()
                        .id(s.getId())
                        .section(s.getSection())
                        .row(s.getRow())
                        .seatNumber(s.getSeatNumber())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public int createByMap(Long venueId, SeatMapRequest request) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new NotFoundException("Venue with id=" + venueId + " not found"));

        List<Seat> toCreate = request.getSections().stream()
                .flatMap(section -> section.getRows().stream()
                        .flatMap(row -> java.util.stream.IntStream.rangeClosed(1, row.getSeatCount())
                                .mapToObj(i -> {
                                    Seat seat = new Seat();
                                    seat.setVenue(venue);
                                    seat.setSection(section.getSectionId());
                                    seat.setRow(row.getRowId());
                                    seat.setSeatNumber(String.valueOf(i));
                                    return seat;
                                })))
                .collect(Collectors.toList());

        seatRepository.saveAll(toCreate);
        return toCreate.size();
    }

    @Override
    @Transactional(readOnly = true)
    public SeatMapResponse getSeatMap(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new NotFoundException("Venue with id=" + venueId + " not found"));

        var seats = seatRepository.findByVenue_IdOrderBySectionAscRowAscSeatNumberAsc(venueId);
        List<SeatMapResponse.Section> sections = SeatMapBuilder.buildSections(seats);

        return SeatMapResponse.builder()
                .venueName(venue.getName())
                .totalCapacity(venue.getCapacity())
                .sections(sections)
                .build();
    }
}