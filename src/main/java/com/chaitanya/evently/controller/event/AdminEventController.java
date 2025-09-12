package com.chaitanya.evently.controller.event;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.request.EventRequest;
import com.chaitanya.evently.dto.event.request.EventStatusChangeRequest;
import com.chaitanya.evently.dto.event.response.EventResponse;
import com.chaitanya.evently.dto.event.response.EventStatusResponse;
import com.chaitanya.evently.service.EventService;
import com.chaitanya.evently.util.SortParameterValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
@Validated
public class AdminEventController {

    private final SortParameterValidator sortResolver;
    private final EventService eventService;

    @Value("${app.pagination.default-page-size:50}")
    private int defaultPageSize;

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request) {
        EventResponse response = eventService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.update(id, request));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<EventStatusResponse> setStatus(@PathVariable Long id,
            @Valid @RequestBody EventStatusChangeRequest request) {
        return ResponseEntity.ok(eventService.setStatus(id, request));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<EventStatusResponse> getStatus(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getStatus(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PaginationResponse<EventResponse>> list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String sort) {

        String resolvedSortParam = sort;
        if (resolvedSortParam == null || resolvedSortParam.isBlank()) {
            resolvedSortParam = "id,asc";
        }

        Pageable pageable;
        if (page == null && size == null) {
            pageable = PageRequest.of(0, defaultPageSize, sortResolver.resolve("events", resolvedSortParam, "id"));
        } else {
            int resolvedPage = page == null ? 0 : page;
            int resolvedSize = size == null ? defaultPageSize : size;
            pageable = PageRequest.of(resolvedPage, resolvedSize,
                    sortResolver.resolve("events", resolvedSortParam, "id"));
        }

        PaginationResponse<EventResponse> response = eventService.list(pageable, resolvedSortParam,
                page != null || size != null);
        return ResponseEntity.ok(response);
    }
}
