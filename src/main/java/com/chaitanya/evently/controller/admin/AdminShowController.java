package com.chaitanya.evently.controller.admin;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.show.ShowFilterRequest;
import com.chaitanya.evently.dto.show.ShowRequest;
import com.chaitanya.evently.dto.show.ShowResponse;
import com.chaitanya.evently.dto.show.ShowStatusUpdateRequest;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.service.ShowService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/shows")
@RequiredArgsConstructor
@Slf4j
public class AdminShowController {

    private final ShowService showService;

    @GetMapping("/{id}")
    public ResponseEntity<ShowResponse> getShowById(@PathVariable Long id) {
        log.info("Admin requested show with id: {}", id);
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
                "Admin requested shows with filters - venueId: {}, eventId: {}, date: {}, fromDate: {}, toDate: {}, pagination: page={}, size={}",
                venueId, eventId, date, fromDate, toDate, paginationRequest.getPage(), paginationRequest.getSize());

        ShowFilterRequest filterRequest = ShowFilterRequest.builder()
                .venueId(venueId)
                .eventId(eventId)
                .date(date != null ? java.time.Instant.parse(date) : null)
                .fromDate(fromDate != null ? java.time.Instant.parse(fromDate) : null)
                .toDate(toDate != null ? java.time.Instant.parse(toDate) : null)
                .build();

        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<ShowResponse> shows = showService.getShowsForAdmin(filterRequest, paginationRequest,
                baseUrl);
        return ResponseEntity.ok(shows);
    }

    @PostMapping
    public ResponseEntity<ShowResponse> createShow(@Valid @RequestBody ShowRequest request) {
        log.info("Admin creating show for event: {} at venue: {}", request.getEventId(), request.getVenueId());
        ShowResponse show = showService.createShow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(show);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShowResponse> updateShow(@PathVariable Long id, @Valid @RequestBody ShowRequest request) {
        log.info("Admin updating show with id: {}", id);
        ShowResponse show = showService.updateShow(id, request);
        return ResponseEntity.ok(show);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ShowResponse> updateShowStatus(@PathVariable Long id,
            @Valid @RequestBody ShowStatusUpdateRequest request) {
        log.info("Admin updating show status to {} for show with id: {}", request.getStatus(), id);
        ShowResponse show = showService.updateShowStatus(id, request);
        return ResponseEntity.ok(show);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShow(@PathVariable Long id) {
        log.info("Admin deleting show with id: {}", id);
        showService.deleteShow(id);
        return ResponseEntity.noContent().build();
    }
}
