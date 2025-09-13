package com.chaitanya.evently.controller.user;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.EventResponse;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/events")
@RequiredArgsConstructor
@Slf4j
public class UserEventController {

    private final EventService eventService;

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        log.info("User requested event with id: {}", id);
        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<PaginationResponse<EventResponse>> getEvents(
            @RequestParam(required = false) String category,
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {
        log.info("User requested events with category: {}, pagination: page={}, size={}",
                category, paginationRequest.getPage(), paginationRequest.getSize());

        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<EventResponse> events = eventService.getEvents(category, paginationRequest, baseUrl);
        return ResponseEntity.ok(events);
    }
}
