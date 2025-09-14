package com.chaitanya.evently.service;

import com.chaitanya.evently.dto.seat.map.SeatMapRequest;
import com.chaitanya.evently.dto.seat.map.SeatMapResponse;
import com.chaitanya.evently.dto.venue.VenueRequest;
import com.chaitanya.evently.exception.types.ConflictException;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.chaitanya.evently.model.Seat;
import com.chaitanya.evently.model.Venue;
import com.chaitanya.evently.repository.SeatRepository;
import com.chaitanya.evently.repository.ShowRepository;
import com.chaitanya.evently.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VenueService {

    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;

    @Transactional(readOnly = true)
    public Venue getVenueById(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venue not found with id: " + id));
        return venue;
    }

    @Transactional(readOnly = true)
    public Venue getVenueByName(String name) {
        Venue venue = venueRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Venue not found with name: " + name));
        return venue;
    }

    @Transactional(readOnly = true)
    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    @Transactional
    public Venue createVenue(VenueRequest request) {
        if (venueRepository.existsByName(request.getName())) {
            throw new ConflictException("Venue with name '" + request.getName() + "' already exists");
        }

        Venue venue = Venue.builder()
                .name(request.getName())
                .address(request.getAddress())
                .capacity(0) // Capacity starts at 0, will be updated when seats are created
                .build();

        Venue savedVenue = venueRepository.save(venue);
        log.info("Created venue with id: {} and name: {}", savedVenue.getId(), savedVenue.getName());

        return savedVenue;
    }

    @Transactional
    public Venue updateVenue(Long id, VenueRequest request) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venue not found with id: " + id));

        // Check if name is being changed and if new name already exists
        if (!venue.getName().equals(request.getName()) && venueRepository.existsByName(request.getName())) {
            throw new ConflictException("Venue with name '" + request.getName() + "' already exists");
        }

        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        // Capacity is not updated through API, only through seat management

        Venue updatedVenue = venueRepository.save(venue);
        log.info("Updated venue with id: {} and name: {}", updatedVenue.getId(), updatedVenue.getName());

        return updatedVenue;
    }

    @Transactional
    public void deleteVenue(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venue not found with id: " + id));

        // Delete all shows first
        showRepository.deleteByVenueId(id);

        // Delete all seats first (this will trigger cascade delete)
        seatRepository.deleteByVenueId(id);

        // Update capacity to 0
        venue.setCapacity(0);
        venueRepository.save(venue);

        venueRepository.delete(venue);
        log.info("Deleted venue with id: {} and name: {} along with all associated shows and seats", venue.getId(),
                venue.getName());
    }

    @Transactional(readOnly = true)
    public SeatMapResponse getSeatMap(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new NotFoundException("Venue not found with id: " + venueId));

        List<Seat> seats = seatRepository.findByVenueIdOrdered(venueId);

        // Group seats by section and row
        Map<String, Map<String, List<Seat>>> groupedSeats = seats.stream()
                .collect(Collectors.groupingBy(
                        Seat::getSection,
                        Collectors.groupingBy(Seat::getRow)));

        List<SeatMapResponse.Section> sections = groupedSeats.entrySet().stream()
                .map(entry -> {
                    String sectionId = entry.getKey();
                    Map<String, List<Seat>> rows = entry.getValue();

                    List<SeatMapResponse.Row> rowList = rows.entrySet().stream()
                            .map(rowEntry -> SeatMapResponse.Row.builder()
                                    .rowId(rowEntry.getKey())
                                    .seats(rowEntry.getValue())
                                    .build())
                            .collect(Collectors.toList());

                    return SeatMapResponse.Section.builder()
                            .sectionId(sectionId)
                            .rows(rowList)
                            .build();
                })
                .collect(Collectors.toList());

        return SeatMapResponse.builder()
                .venueName(venue.getName())
                .totalCapacity(venue.getCapacity())
                .sections(sections)
                .build();
    }

    @Transactional
    public SeatMapResponse createSeatMap(Long venueId, SeatMapRequest request) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new NotFoundException("Venue not found with id: " + venueId));

        // Get current seat count for capacity calculation
        long currentSeatCount = seatRepository.countByVenueId(venueId);

        // Create new seats based on the hierarchical structure
        List<Seat> seats = new ArrayList<>();

        for (SeatMapRequest.Section section : request.getSections()) {
            for (SeatMapRequest.Row row : section.getRows()) {
                for (int i = 1; i <= row.getSeatCount(); i++) {
                    String seatNumber = String.valueOf(i);

                    // Check for duplicate seats - if exists, skip this seat
                    if (seatRepository.existsByVenueIdAndSectionAndRowAndSeatNumber(
                            venueId, section.getSectionId(), row.getRowId(), seatNumber)) {
                        log.warn("Seat already exists, skipping: {} - {} - {}",
                                section.getSectionId(), row.getRowId(), seatNumber);
                        continue; // Skip this seat instead of throwing exception
                    }

                    Seat seat = Seat.builder()
                            .venue(venue)
                            .section(section.getSectionId())
                            .row(row.getRowId())
                            .seatNumber(seatNumber)
                            .build();
                    seats.add(seat);
                }
            }
        }

        // Only save seats if there are new ones to create
        if (!seats.isEmpty()) {
            List<Seat> savedSeats = seatRepository.saveAll(seats);
            log.info("Created {} new seats for venue with id: {}", savedSeats.size(), venueId);

            // Update venue capacity by adding new seats to existing count
            long newTotalCapacity = currentSeatCount + savedSeats.size();
            venue.setCapacity((int) newTotalCapacity);
            venueRepository.save(venue);
            log.info("Updated venue capacity to {} for venue with id: {}", newTotalCapacity, venueId);
        } else {
            log.info("No new seats to create for venue with id: {} (all requested seats already exist)", venueId);
        }

        // Return the seat map in the same hierarchical structure
        return getSeatMap(venueId);
    }

    /**
     * Recalculates and updates the venue capacity based on the number of seats
     */
    @Transactional
    public void recalculateVenueCapacity(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new NotFoundException("Venue not found with id: " + venueId));

        long seatCount = seatRepository.countByVenueId(venueId);
        venue.setCapacity((int) seatCount);
        venueRepository.save(venue);

        log.info("Recalculated venue capacity to {} for venue with id: {}", seatCount, venueId);
    }
}
