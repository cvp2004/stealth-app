package com.chaitanya.evently.controller.user;

import com.chaitanya.evently.dto.seat.map.SeatMapResponse;
import com.chaitanya.evently.model.Venue;
import com.chaitanya.evently.service.VenueService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/venue")
@RequiredArgsConstructor
@Slf4j
public class UserVenueController {

    private final VenueService venueService;

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getAllVenues() {
        log.info("User requested all venues");
        List<Venue> venues = venueService.getAllVenues();
        List<Map<String, Object>> response = venues.stream()
                .map(this::toVenueResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getVenueById(@PathVariable Long id) {
        log.info("User requested venue with id: {}", id);
        Venue venue = venueService.getVenueById(id);
        return ResponseEntity.ok(toVenueResponse(venue));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Map<String, Object>> getVenueByName(@PathVariable String name) {
        log.info("User requested venue with name: {}", name);
        Venue venue = venueService.getVenueByName(name);
        return ResponseEntity.ok(toVenueResponse(venue));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<SeatMapResponse> getSeatMap(@PathVariable Long id) {
        log.info("User requested seat map for venue with id: {}", id);
        SeatMapResponse seatMap = venueService.getSeatMap(id);
        return ResponseEntity.ok(seatMap);
    }

    private Map<String, Object> toVenueResponse(Venue venue) {
        return Map.of(
                "id", venue.getId(),
                "name", venue.getName(),
                "address", venue.getAddress(),
                "capacity", venue.getCapacity());
    }
}
