package com.chaitanya.evently.controller.user;

import com.chaitanya.evently.dto.seat.map.SeatMapResponse;
import com.chaitanya.evently.dto.venue.VenueResponse;
import com.chaitanya.evently.service.VenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/venues")
@RequiredArgsConstructor
@Slf4j
public class UserVenueController {

    private final VenueService venueService;

    @GetMapping
    public ResponseEntity<List<VenueResponse>> getAllVenues() {
        log.info("User requested all venues");
        List<VenueResponse> venues = venueService.getAllVenues();
        return ResponseEntity.ok(venues);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueResponse> getVenueById(@PathVariable Long id) {
        log.info("User requested venue with id: {}", id);
        VenueResponse venue = venueService.getVenueById(id);
        return ResponseEntity.ok(venue);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<VenueResponse> getVenueByName(@PathVariable String name) {
        log.info("User requested venue with name: {}", name);
        VenueResponse venue = venueService.getVenueByName(name);
        return ResponseEntity.ok(venue);
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<SeatMapResponse> getSeatMap(@PathVariable Long id) {
        log.info("User requested seat map for venue with id: {}", id);
        SeatMapResponse seatMap = venueService.getSeatMap(id);
        return ResponseEntity.ok(seatMap);
    }
}
