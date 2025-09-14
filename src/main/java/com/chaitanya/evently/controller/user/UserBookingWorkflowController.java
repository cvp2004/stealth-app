package com.chaitanya.evently.controller.user;

import com.chaitanya.evently.dto.booking.BookingCreateRequest;
import com.chaitanya.evently.dto.booking.BookingCreateResponse;
import com.chaitanya.evently.dto.booking.BookingPaymentRequest;
import com.chaitanya.evently.dto.booking.BookingPaymentResponse;
import com.chaitanya.evently.dto.show.ShowSeatsResponse;
import com.chaitanya.evently.service.BookingWorkflowService;
import com.chaitanya.evently.util.HeaderUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserBookingWorkflowController {

    private final BookingWorkflowService bookingWorkflowService;

    @GetMapping("/show/{showId}/seats")
    public ResponseEntity<Map<String, Object>> getShowSeats(@PathVariable Long showId) {
        log.info("User requested seat map for show: {}", showId);
        ShowSeatsResponse response = bookingWorkflowService.getShowSeats(showId);
        return ResponseEntity.ok(toShowSeatsResponse(response));
    }

    @PostMapping("/booking")
    public ResponseEntity<BookingCreateResponse> createBooking(
            @Valid @RequestBody BookingCreateRequest request,
            HttpServletRequest httpRequest) {

        // Get userId from header for authentication
        Long userId = HeaderUtil.getUserIdFromHeader(httpRequest);

        log.info("User {} requested booking for show {} with {} seats",
                userId, request.getShowId(), request.getSeats().size());

        BookingCreateResponse response = bookingWorkflowService.createBooking(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/booking/payment")
    public ResponseEntity<BookingPaymentResponse> processPayment(
            @Valid @RequestBody BookingPaymentRequest request,
            HttpServletRequest httpRequest) {

        // Get userId from header for authentication
        Long userId = HeaderUtil.getUserIdFromHeader(httpRequest);

        log.info("User {} processing payment for reservation: {}", userId, request.getReservationId());

        BookingPaymentResponse response = bookingWorkflowService.processPayment(request, userId);
        return ResponseEntity.ok(response);
    }

    // Private Mapper Methods

    private Map<String, Object> toShowSeatsResponse(ShowSeatsResponse response) {
        return Map.of(
                "showId", response.getShowId(),
                "showName", response.getShowName(),
                "eventName", response.getEventName(),
                "venueName", response.getVenueName(),
                "bookedSeatIds",
                response.getBookedSeatIds() == null ? List.of() : response.getBookedSeatIds(),
                "seatMap", mapSeatMap(response));
    }

    private Map<String, Object> mapSeatMap(ShowSeatsResponse response) {
        if (response.getSeatMap() == null) {
            return Map.of();
        }
        Set<Long> booked = new HashSet<>(
                response.getBookedSeatIds() == null ? List.of() : response.getBookedSeatIds());
        return Map.of(
                "venueName", response.getSeatMap().getVenueName(),
                "totalCapacity", response.getSeatMap().getTotalCapacity(),
                "sections", mapSections(response, booked));
    }

    private List<Map<String, Object>> mapSections(ShowSeatsResponse response,
            Set<Long> booked) {
        var sections = response.getSeatMap().getSections();
        if (sections == null) {
            return List.of();
        }
        return sections.stream()
                .map(section -> Map.of(
                        "sectionId", section.getSectionId(),
                        "rows", mapRows(section, booked)))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> mapRows(
            com.chaitanya.evently.dto.seat.map.SeatMapResponse.Section section, Set<Long> booked) {
        var rows = section.getRows();
        if (rows == null) {
            return List.of();
        }
        return rows.stream()
                .map(row -> Map.of(
                        "rowId", row.getRowId(),
                        "seats", mapSeats(row, booked)))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> mapSeats(
            com.chaitanya.evently.dto.seat.map.SeatMapResponse.Row row, Set<Long> booked) {
        var seats = row.getSeats();
        if (seats == null) {
            return List.of();
        }
        return seats.stream()
                .map(seat -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", seat.getId());
                    m.put("seat_label", seat.getSection() + "-" + seat.getRow() + "-" + seat.getSeatNumber());
                    boolean isBooked = seat.getId() != null && booked.contains(seat.getId());
                    m.put("status", isBooked ? "BOOKED" : "AVAILABLE");
                    return m;
                })
                .collect(Collectors.toList());
    }
}
