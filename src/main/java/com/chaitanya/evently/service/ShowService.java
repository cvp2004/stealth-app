package com.chaitanya.evently.service;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.show.request.ShowRequest;
import com.chaitanya.evently.dto.show.response.ShowResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface ShowService {
    ShowResponse create(ShowRequest request);

    ShowResponse get(Long id);

    ShowResponse update(Long id, ShowRequest request);

    void delete(Long id);

    PaginationResponse<ShowResponse> list(
            Pageable pageable,
            String sort,
            boolean isPaginated,
            Long venueId,
            Long eventId,
            Instant date,
            Instant fromDate,
            Instant toDate);
}
