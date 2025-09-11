package com.chaitanya.evently.service.impl;

import com.chaitanya.evently.dto.seat.SeatResponse;
import com.chaitanya.evently.dto.seat.map.SeatMapRequest;
import com.chaitanya.evently.dto.seat.map.SeatMapResponse;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.chaitanya.evently.model.Seat;
import com.chaitanya.evently.model.Venue;
import com.chaitanya.evently.repository.SeatRepository;
import com.chaitanya.evently.repository.projection.SeatFlatProjection;
import com.chaitanya.evently.repository.VenueRepository;
import com.chaitanya.evently.service.SeatService;
import java.util.ArrayList;
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

        List<Seat> toCreate = new ArrayList<>();
        for (SeatMapRequest.Section section : request.getSections()) {
            for (SeatMapRequest.Row row : section.getRows()) {
                int count = row.getSeatCount();
                for (int i = 1; i <= count; i++) {
                    Seat seat = new Seat();
                    seat.setVenue(venue);
                    seat.setSection(section.getSectionId());
                    seat.setRow(row.getRowId());
                    seat.setSeatNumber(String.valueOf(i));
                    toCreate.add(seat);
                }
            }
        }
        seatRepository.saveAll(toCreate);
        return toCreate.size();
    }

    @Override
    @Transactional(readOnly = true)
    public SeatMapResponse getSeatMap(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new NotFoundException("Venue with id=" + venueId + " not found"));

        var seats = seatRepository.findByVenue_IdOrderBySectionAscRowAscSeatNumberAsc(venueId);
        var sectionsGrouped = seats.stream()
                .collect(Collectors.groupingBy(SeatFlatProjection::section, Collectors.groupingBy(SeatFlatProjection::row)));

        List<SeatMapResponse.Section> sections = new ArrayList<>();
        for (var secEntry : sectionsGrouped.entrySet()) {
            List<SeatMapResponse.Row> rows = new ArrayList<>();
            for (var rowEntry : secEntry.getValue().entrySet()) {
                List<SeatResponse> seatDtos = rowEntry.getValue().stream()
                        .map(s -> SeatResponse.builder()
                                .id(s.id())
                                .section(s.section())
                                .row(s.row())
                                .seatNumber(s.seatNumber())
                                .build())
                        .toList();
                rows.add(SeatMapResponse.Row.builder().rowId(rowEntry.getKey()).seats(seatDtos).build());
            }
            sections.add(SeatMapResponse.Section.builder().sectionId(secEntry.getKey()).rows(rows).build());
        }

        return SeatMapResponse.builder()
                .venueName(venue.getName())
                .totalCapacity(venue.getCapacity())
                .sections(sections)
                .build();
    }
}