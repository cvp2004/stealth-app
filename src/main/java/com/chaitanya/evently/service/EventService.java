package com.chaitanya.evently.service;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.request.EventRequest;
import com.chaitanya.evently.dto.event.request.EventStatusChangeRequest;
import com.chaitanya.evently.dto.event.response.EventResponse;
import com.chaitanya.evently.dto.event.response.EventStatusResponse;
import org.springframework.data.domain.Pageable;

public interface EventService {
    EventResponse create(EventRequest request);

    EventResponse get(Long id);

    EventResponse update(Long id, EventRequest request);

    EventStatusResponse setStatus(Long id, EventStatusChangeRequest request);

    EventStatusResponse getStatus(Long id);

    void delete(Long id);

    PaginationResponse<EventResponse> list(
            Pageable pageable,
            String sortParam,
            boolean isPaginated);
}
