package com.chaitanya.evently.controller.user;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.show.ShowFilterRequest;
import com.chaitanya.evently.dto.show.ShowResponse;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.service.ShowService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/shows")
@RequiredArgsConstructor
@Slf4j
public class UserShowController {

    private final ShowService showService;

    @GetMapping("/{id}")
    public ResponseEntity<ShowResponse> getShowById(@PathVariable Long id) {
        log.info("User requested show with id: {}", id);
        ShowResponse show = showService.getShowById(id);
        return ResponseEntity.ok(show);
    }

    @GetMapping
    public ResponseEntity<PaginationResponse<ShowResponse>> getShows(
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {

        log.info(
                "User requested shows with filters - venueId: {}, eventId: {}, date: {}, fromDate: {}, toDate: {}, pagination: page={}, size={}",
                venueId, eventId, date, fromDate, toDate, paginationRequest.getPage(), paginationRequest.getSize());

        ShowFilterRequest filterRequest = ShowFilterRequest.builder()
                .venueId(venueId)
                .eventId(eventId)
                .date(date != null ? java.time.Instant.parse(date) : null)
                .fromDate(fromDate != null ? java.time.Instant.parse(fromDate) : null)
                .toDate(toDate != null ? java.time.Instant.parse(toDate) : null)
                .build();

        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<ShowResponse> shows = showService.getShows(filterRequest, paginationRequest, baseUrl);
        return ResponseEntity.ok(shows);
    }
}
