package com.chaitanya.evently.controller.admin;

import com.chaitanya.evently.dto.seat.map.SeatMapRequest;
import com.chaitanya.evently.dto.seat.map.SeatMapResponse;
import com.chaitanya.evently.dto.venue.VenueRequest;
import com.chaitanya.evently.dto.venue.VenueResponse;
import com.chaitanya.evently.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/venues")
@RequiredArgsConstructor
@Slf4j
public class AdminVenueController {

    private final VenueService venueService;

    @GetMapping
    public ResponseEntity<List<VenueResponse>> getAllVenues() {
        log.info("Admin requested all venues");
        List<VenueResponse> venues = venueService.getAllVenues();
        return ResponseEntity.ok(venues);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueResponse> getVenueById(@PathVariable Long id) {
        log.info("Admin requested venue with id: {}", id);
        VenueResponse venue = venueService.getVenueById(id);
        return ResponseEntity.ok(venue);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<VenueResponse> getVenueByName(@PathVariable String name) {
        log.info("Admin requested venue with name: {}", name);
        VenueResponse venue = venueService.getVenueByName(name);
        return ResponseEntity.ok(venue);
    }

    @PostMapping
    public ResponseEntity<VenueResponse> createVenue(@Valid @RequestBody VenueRequest request) {
        log.info("Admin creating venue with name: {}", request.getName());
        VenueResponse venue = venueService.createVenue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(venue);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VenueResponse> updateVenue(@PathVariable Long id, @Valid @RequestBody VenueRequest request) {
        log.info("Admin updating venue with id: {}", id);
        VenueResponse venue = venueService.updateVenue(id, request);
        return ResponseEntity.ok(venue);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long id) {
        log.info("Admin deleting venue with id: {}", id);
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<SeatMapResponse> getSeatMap(@PathVariable Long id) {
        log.info("Admin requested seat map for venue with id: {}", id);
        SeatMapResponse seatMap = venueService.getSeatMap(id);
        return ResponseEntity.ok(seatMap);
    }

    @PostMapping("/{id}/seats")
    public ResponseEntity<SeatMapResponse> createSeatMap(@PathVariable Long id,
            @Valid @RequestBody SeatMapRequest request) {
        log.info("Admin creating seat map for venue with id: {} with {} sections",
                id, request.getSections().size());
        SeatMapResponse seatMap = venueService.createSeatMap(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(seatMap);
    }
}
