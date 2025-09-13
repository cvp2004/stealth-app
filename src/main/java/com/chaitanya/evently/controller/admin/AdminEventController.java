package com.chaitanya.evently.controller.admin;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.EventRequest;
import com.chaitanya.evently.dto.event.EventResponse;
import com.chaitanya.evently.dto.event.EventStatusUpdateRequest;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/events")
@RequiredArgsConstructor
@Slf4j
public class AdminEventController {

    private final EventService eventService;

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        log.info("Admin requested event with id: {}", id);
        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<PaginationResponse<EventResponse>> getEvents(
            @RequestParam(required = false) String category,
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {
        log.info("Admin requested events with category: {}, pagination: page={}, size={}",
                category, paginationRequest.getPage(), paginationRequest.getSize());

        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<EventResponse> events = eventService.getEventsForAdmin(category, paginationRequest, baseUrl);
        return ResponseEntity.ok(events);
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request) {
        log.info("Admin creating event with title: {}", request.getTitle());
        EventResponse event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        log.info("Admin updating event with id: {}", id);
        EventResponse event = eventService.updateEvent(id, request);
        return ResponseEntity.ok(event);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EventResponse> updateEventStatus(@PathVariable Long id,
            @Valid @RequestBody EventStatusUpdateRequest request) {
        log.info("Admin updating event status to {} for event with id: {}", request.getStatus(), id);
        EventResponse event = eventService.updateEventStatus(id, request);
        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        log.info("Admin deleting event with id: {}", id);
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
