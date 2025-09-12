package com.chaitanya.evently.controller.show;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.show.request.ShowFilterRequest;
import com.chaitanya.evently.dto.show.request.ShowRequest;
import com.chaitanya.evently.dto.show.response.ShowResponse;
import com.chaitanya.evently.service.ShowService;
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
@RequestMapping("/api/v1/shows")
@Validated
public class AdminShowController {

    private final ShowService showService;
    private final SortParameterValidator sortResolver;

    @Value("${app.pagination.default-page-size:50}")
    private int defaultPageSize;

    @PostMapping
    public ResponseEntity<ShowResponse> create(@Valid @RequestBody ShowRequest request) {
        ShowResponse response = showService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(showService.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShowResponse> update(@PathVariable Long id, @Valid @RequestBody ShowRequest request) {
        return ResponseEntity.ok(showService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        showService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PaginationResponse<ShowResponse>> list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String sort,
            @Valid @RequestBody(required = false) ShowFilterRequest request) {

        String resolvedSortParam = sort != null && !sort.isBlank()
                ? sort
                : "startTimestamp,asc";

        Pageable pageable;
        if (page == null && size == null) {
            pageable = PageRequest.of(0, defaultPageSize,
                    sortResolver.resolve("shows", resolvedSortParam, "startTimestamp"));
        } else {
            int pageNum = page == null ? 0 : page;
            int pageSize = size == null ? defaultPageSize : size;
            pageable = PageRequest.of(pageNum, pageSize,
                    sortResolver.resolve("shows", resolvedSortParam, "startTimestamp"));
        }

        // Use request body filters if provided, otherwise use nulls
        Long venueId = request != null ? request.getVenueId() : null;
        Long eventId = request != null ? request.getEventId() : null;
        java.time.Instant date = request != null ? request.getDate() : null;
        java.time.Instant fromDate = request != null ? request.getFromDate() : null;
        java.time.Instant toDate = request != null ? request.getToDate() : null;

        PaginationResponse<ShowResponse> response = showService.list(
                pageable, resolvedSortParam, page != null || size != null,
                venueId, eventId, date, fromDate, toDate);

        return ResponseEntity.ok(response);
    }
}
