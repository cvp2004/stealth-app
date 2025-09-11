package com.chaitanya.evently.service;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.venue.VenueDtos.VenueRequest;
import com.chaitanya.evently.dto.venue.VenueDtos.VenueResponse;
import org.springframework.data.domain.Pageable;

public interface VenueService {
    VenueResponse create(VenueRequest request);

    VenueResponse update(Long id, VenueRequest request);

    VenueResponse get(Long id);

    void delete(Long id);

    PaginationResponse<VenueResponse> list(Pageable pageable, String sort, boolean forcePaginated);
}
