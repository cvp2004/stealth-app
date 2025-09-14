package com.chaitanya.evently.controller.admin;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.EventRequest;
import com.chaitanya.evently.dto.event.EventStatusUpdateRequest;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.model.Event;
import com.chaitanya.evently.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/event")
@RequiredArgsConstructor
@Slf4j
public class AdminEventController {

    private final EventService eventService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getEvents(
            @RequestParam(required = false) String category,
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {
        log.info("Admin requested events with category: {}, pagination: page={}, size={}",
                category, paginationRequest.getPage(), paginationRequest.getSize());

        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<Event> events = eventService.getEventsForAdmin(category, paginationRequest, baseUrl);
        return ResponseEntity.ok(toPaginationResponseMap(events));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEventById(@PathVariable Long id) {
        log.info("Admin requested event with id: {}", id);
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(toEventResponse(event));
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<Map<String, Object>> getEventByTitle(@PathVariable String title) {
        log.info("Admin requested event with title: {}", title);
        Event event = eventService.getEventByTitleForAdmin(title);
        return ResponseEntity.ok(toEventResponse(event));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createEvent(@Valid @RequestBody EventRequest request) {
        log.info("Admin creating event with title: {}", request.getTitle());
        Event event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toEventResponse(event));
    }

    @PatchMapping("/{id}/status/update")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody EventStatusUpdateRequest request) {
        log.info("Admin updating event status to {} for event with id: {}", request.getStatus(), id);
        Event event = eventService.updateEventStatus(id, request);
        return ResponseEntity.ok(toEventResponse(event));
    }

    // Private Mapper Methods

    private Map<String, Object> toEventResponse(Event event) {
        return Map.of(
                "id", event.getId(),
                "title", event.getTitle(),
                "description", event.getDescription(),
                "category", event.getCategory(),
                "status", String.valueOf(event.getStatus()));
    }

    private Map<String, Object> toPaginationResponseMap(PaginationResponse<Event> response) {
        List<Map<String, Object>> content = response.getContent() == null ? List.of()
                : response.getContent().stream()
                        .map(this::toEventResponse)
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
                        "self", response.getLinks().getSelf(),
                        "first", response.getLinks().getFirst(),
                        "last", response.getLinks().getLast(),
                        "next", response.getLinks().getNext(),
                        "prev", response.getLinks().getPrev());

        return Map.of(
                "isPaginated", response.isPaginated(),
                "content", content,
                "page", page,
                "sort", sort,
                "links", links);
    }
}
