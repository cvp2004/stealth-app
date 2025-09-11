package com.chaitanya.evently.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import com.chaitanya.evently.dto.seat.map.SeatMapRequest;
import com.chaitanya.evently.dto.seat.map.SeatMapResponse;
import com.chaitanya.evently.dto.seat.BulkSeatCreationResponse;
import com.chaitanya.evently.service.SeatService;
import com.chaitanya.evently.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/venues/{venueId}/seats")
@Validated
public class AdminVenueSeatController {

    private final SeatService seatService;
    private final VenueService venueService;

    @PostMapping
    public ResponseEntity<BulkSeatCreationResponse> createSeats(
            @PathVariable Long venueId,
            @Valid @RequestBody SeatMapRequest seatMap) {
        int created = seatService.createByMap(venueId, seatMap);
        var venue = venueService.get(venueId);
        BulkSeatCreationResponse body = BulkSeatCreationResponse.builder()
                .seatCount(created)
                .venueName(venue.getName())
                .totalCapacity(venue.getCapacity())
                .sectionCount(seatMap != null && seatMap.getSections() != null ? seatMap.getSections().size() : 0)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    public ResponseEntity<SeatMapResponse> listSeats(@PathVariable Long venueId) {
        return ResponseEntity.ok(seatService.getSeatMap(venueId));
    }
}
