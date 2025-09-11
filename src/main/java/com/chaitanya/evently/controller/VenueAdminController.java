package com.chaitanya.evently.controller;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.util.SortParameterValidator;
import com.chaitanya.evently.dto.venue.VenueDtos.VenueRequest;
import com.chaitanya.evently.dto.venue.VenueDtos.VenueResponse;
import com.chaitanya.evently.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/venues")
@Validated
public class VenueAdminController {

    private final VenueService venueService;
    private final SortParameterValidator sortResolver;
    @Value("${app.pagination.default-page-size:50}")
    private int defaultPageSize;

    @PostMapping
    public ResponseEntity<VenueResponse> create(@Valid @RequestBody VenueRequest request) {
        VenueResponse response = venueService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VenueResponse> update(@PathVariable Long id, @Valid @RequestBody VenueRequest request) {
        return ResponseEntity.ok(venueService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.get(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        venueService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PaginationResponse<VenueResponse>> list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String sort,
            @Pattern(regexp = "(?i)asc|desc", message = "direction must be 'asc' or 'desc'", flags = {}) @RequestParam(value = "direction", required = false) String direction) {

        String resolvedSortParam = sort;
        if (direction != null && !direction.isBlank()) {
            String dir = direction.toLowerCase();
            String property = (sort == null || sort.isBlank()) ? "name" : sort;
            resolvedSortParam = property + "," + dir;
        }

        Pageable pageable;
        if (page == null && size == null) {
            pageable = PageRequest.of(0, defaultPageSize, sortResolver.resolve("venues", resolvedSortParam, "name"));
        } else {
            int resolvedPage = page == null ? 0 : page;
            int resolvedSize = size == null ? defaultPageSize : size;
            pageable = PageRequest.of(resolvedPage, resolvedSize,
                    sortResolver.resolve("venues", resolvedSortParam, "name"));
        }
        PaginationResponse<VenueResponse> response = venueService.list(pageable, resolvedSortParam,
                page != null || size != null);
        return ResponseEntity.ok(response);
    }
}
