package com.chaitanya.evently.controller.admin;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.dto.show.ShowRequest;
import com.chaitanya.evently.dto.show.ShowStatusUpdateRequest;
import com.chaitanya.evently.model.Show;
import com.chaitanya.evently.model.Event;
import com.chaitanya.evently.model.Venue;
import com.chaitanya.evently.service.ShowService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/show")
@RequiredArgsConstructor
@Slf4j
public class AdminShowController {

    private final ShowService showService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createShow(@Valid @RequestBody ShowRequest request) {
        log.info("Admin creating show for event: {} at venue: {}", request.getEventId(), request.getVenueId());
        Show show = showService.createShow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toShowResponse(show));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getShowById(@PathVariable Long id) {
        log.info("Admin requested show with id: {}", id);
        Show show = showService.getShowById(id);
        return ResponseEntity.ok(toShowResponse(show));
    }

    @GetMapping("/venue/{venueId}/list")
    public ResponseEntity<Map<String, Object>> getShowsByVenueId(
            @PathVariable Long venueId,
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {

        log.info("Admin requested shows for venueId: {} - pagination: page={}, size={}",
                venueId, paginationRequest.getPage(), paginationRequest.getSize());

        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<Show> shows = showService.getShowsByVenueId(venueId, paginationRequest, baseUrl);
        return ResponseEntity.ok(toPaginationResponseMap(shows));
    }

    @GetMapping("/event/{eventId}/list")
    public ResponseEntity<Map<String, Object>> getShowsByEventId(
            @PathVariable Long eventId,
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {

        log.info("Admin requested shows for eventId: {} - pagination: page={}, size={}",
                eventId, paginationRequest.getPage(), paginationRequest.getSize());

        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<Show> shows = showService.getShowsByEventId(eventId, paginationRequest, baseUrl);
        return ResponseEntity.ok(toPaginationResponseMap(shows));
    }

    @PatchMapping("/{id}/status/update")
    public ResponseEntity<Map<String, Object>> updateShowStatus(@PathVariable Long id,
            @Valid @RequestBody ShowStatusUpdateRequest request) {
        log.info("Admin updating show status to {} for show with id: {}", request.getStatus(), id);
        Show show = showService.updateShowStatus(id, request);
        return ResponseEntity.ok(toShowResponse(show));
    }

    // Private Helper Methods

    private Map<String, Object> toShowResponse(Show show) {
        return Map.of(
                "id", show.getId(),
                "venue", toVenueResponse(show.getVenue()),
                "event", toEventResponse(show.getEvent()),
                "startTimestamp", show.getStartTimestamp(),
                "durationMinutes", show.getDurationMinutes(),
                "status", String.valueOf(show.getStatus()));
    }

    private Map<String, Object> toEventResponse(Event event) {
        return Map.of(
                "id", event.getId(),
                "title", event.getTitle(),
                "description", event.getDescription(),
                "category", event.getCategory());
    }

    private Map<String, Object> toVenueResponse(Venue venue) {
        return Map.of(
                "id", venue.getId(),
                "name", venue.getName(),
                "address", venue.getAddress());
    }

    private Map<String, Object> toPaginationResponseMap(PaginationResponse<Show> response) {
        List<Map<String, Object>> content = response.getContent() == null ? List.of()
                : response.getContent().stream()
                        .map(this::toShowResponse)
                        .collect(Collectors.toList());

        Map<String, Object> page = response.getPage() == null ? Map.of()
                : Map.of(
                        "number", response.getPage().getNumber(),
                        "size", response.getPage().getSize(),
                        "totalElements", response.getPage().getTotalElements(),
                        "totalPages", response.getPage().getTotalPages());

        List<Map<String, Object>> sortFields = (response.getSort() == null || response.getSort().getFields() == null)
                ? List.of()
                : response.getSort().getFields().stream()
                        .map(f -> Map.<String, Object>of(
                                "property", f.getProperty(),
                                "direction", f.getDirection()))
                        .collect(Collectors.toList());

        Map<String, Object> sort = Map.of("fields", sortFields);

        Map<String, Object> links = response.getLinks() == null ? Map.of()
                : Map.of(
                        "self", Optional.ofNullable(response.getLinks().getSelf()),
                        "first", Optional.ofNullable(response.getLinks().getFirst()),
                        "last", Optional.ofNullable(response.getLinks().getLast()),
                        "next", Optional.ofNullable(response.getLinks().getNext()),
                        "prev", Optional.ofNullable(response.getLinks().getPrev()));

        return Map.of(
                "isPaginated", response.isPaginated(),
                "content", content,
                "page", page,
                "sort", sort,
                "links", links);
    }
}
