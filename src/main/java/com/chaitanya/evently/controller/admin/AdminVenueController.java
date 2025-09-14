package com.chaitanya.evently.controller.admin;

import com.chaitanya.evently.dto.seat.map.SeatMapRequest;
import com.chaitanya.evently.dto.seat.map.SeatMapResponse;
import com.chaitanya.evently.dto.venue.VenueRequest;
import com.chaitanya.evently.model.Venue;
import com.chaitanya.evently.model.Seat;
import com.chaitanya.evently.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/venue")
@RequiredArgsConstructor
@Slf4j
public class AdminVenueController {

    private final VenueService venueService;

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getAllVenues() {
        log.info("Admin requested all venues");
        List<Venue> venues = venueService.getAllVenues();
        List<Map<String, Object>> response = venues.stream()
                .map(this::toVenueResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getVenueById(@PathVariable Long id) {
        log.info("Admin requested venue with id: {}", id);
        Venue venue = venueService.getVenueById(id);
        return ResponseEntity.ok(toVenueResponse(venue));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Map<String, Object>> getVenueByName(@PathVariable String name) {
        log.info("Admin requested venue with name: {}", name);
        Venue venue = venueService.getVenueByName(name);
        return ResponseEntity.ok(toVenueResponse(venue));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createVenue(@Valid @RequestBody VenueRequest request) {
        log.info("Admin creating venue with name: {}", request.getName());
        Venue venue = venueService.createVenue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toVenueResponse(venue));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<Map<String, Object>> getSeatMap(@PathVariable Long id) {
        log.info("Admin requested seat map for venue with id: {}", id);
        SeatMapResponse seatMap = venueService.getSeatMap(id);
        return ResponseEntity.ok(toSeatMapResponse(seatMap));
    }

    @PostMapping("/{id}/seats")
    public ResponseEntity<Map<String, Object>> createSeatMap(@PathVariable Long id,
            @Valid @RequestBody SeatMapRequest request) {
        log.info("Admin creating seat map for venue with id: {} with {} sections",
                id, request.getSections().size());
        SeatMapResponse seatMap = venueService.createSeatMap(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toSeatMapResponse(seatMap));
    }

    // Mapper Methods

    private Map<String, Object> toVenueResponse(Venue venue) {
        return Map.of(
                "id", venue.getId(),
                "name", venue.getName(),
                "address", venue.getAddress(),
                "capacity", venue.getCapacity());
    }

    private Map<String, Object> toSeatMapResponse(SeatMapResponse seatMap) {
        return Map.of(
                "venueName", seatMap.getVenueName(),
                "totalCapacity", seatMap.getTotalCapacity(),
                "sections", mapSections(seatMap.getSections()));
    }

    private List<Map<String, Object>> mapSections(List<SeatMapResponse.Section> sections) {
        if (sections == null) {
            return List.of();
        }
        return sections.stream()
                .map(this::mapSection)
                .collect(Collectors.toList());
    }

    private Map<String, Object> mapSection(SeatMapResponse.Section section) {
        return Map.of(
                "sectionId", section.getSectionId(),
                "rows", mapRows(section.getRows()));
    }

    private List<Map<String, Object>> mapRows(List<SeatMapResponse.Row> rows) {
        if (rows == null) {
            return List.of();
        }
        return rows.stream()
                .map(this::mapRow)
                .collect(Collectors.toList());
    }

    private Map<String, Object> mapRow(SeatMapResponse.Row row) {
        return Map.of(
                "rowId", row.getRowId(),
                "seats", mapSeats(row.getSeats()));
    }

    private List<Map<String, Object>> mapSeats(List<Seat> seats) {
        if (seats == null) {
            return List.of();
        }
        return seats.stream()
                .map(this::mapSeat)
                .collect(Collectors.toList());
    }

    private Map<String, Object> mapSeat(Seat seat) {
        return Map.of(
                "id", seat.getId(),
                "seat_label", seat.getSection() + "-" + seat.getRow() + "-" + seat.getSeatNumber(),
                "section", seat.getSection(),
                "row", seat.getRow(),
                "seat", seat.getSeatNumber());
    }
}
