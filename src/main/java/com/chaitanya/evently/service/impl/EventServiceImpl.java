package com.chaitanya.evently.service.impl;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.request.EventRequest;
import com.chaitanya.evently.dto.event.request.EventStatusChangeRequest;
import com.chaitanya.evently.dto.event.response.EventResponse;
import com.chaitanya.evently.dto.event.response.EventStatusResponse;
import com.chaitanya.evently.exception.types.BadRequestException;
import com.chaitanya.evently.exception.types.ConflictException;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.chaitanya.evently.model.Event;
import com.chaitanya.evently.model.status.EventStatus;
import com.chaitanya.evently.repository.EventRepository;
import com.chaitanya.evently.service.EventService;
import com.chaitanya.evently.util.ReferenceIdFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ReferenceIdFormatter referenceIdFormatter;
    private static final String EVENT_NOT_FOUND = "Event not found";

    @Override
    public EventResponse create(EventRequest request) {
        if (eventRepository.existsByTitleIgnoreCase(request.getTitle())) {
            throw new ConflictException("Event with title '" + request.getTitle() + "' already exists");
        }
        Event entity = new Event();
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setCategory(request.getCategory());
        entity.setStatus(EventStatus.CREATED);
        Event saved = eventRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    public EventResponse get(Long id) {
        Event entity = eventRepository.findById(id).orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND));
        return toResponse(entity);
    }

    @Override
    public EventResponse update(Long id, EventRequest request) {
        Event entity = eventRepository.findById(id).orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND));
        ensureUpdatable(entity);
        if (!entity.getTitle().equalsIgnoreCase(request.getTitle()) &&
                eventRepository.existsByTitleIgnoreCase(request.getTitle())) {
            throw new ConflictException("Event with title '" + request.getTitle() + "' already exists");
        }
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setCategory(request.getCategory());
        Event saved = eventRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    public EventStatusResponse setStatus(Long id, EventStatusChangeRequest request) {
        Event entity = eventRepository.findById(id).orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND));
        EventStatus from = entity.getStatus();
        EventStatus to = request.getStatus();
        validateTransition(from, to);
        entity.setStatus(to);
        Event saved = eventRepository.save(entity);
        String refId = referenceIdFormatter.format("EVT", saved.getId(), 6);
        return EventStatusResponse.builder()
                .id(saved.getId())
                .refId(refId)
                .title(saved.getTitle())
                .status(saved.getStatus())
                .build();
    }

    @Override
    public EventStatusResponse getStatus(Long id) {
        Event entity = eventRepository.findById(id).orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND));
        String refId = referenceIdFormatter.format("EVT", entity.getId(), 6);
        return EventStatusResponse.builder()
                .id(entity.getId())
                .refId(refId)
                .title(entity.getTitle())
                .status(entity.getStatus())
                .build();
    }

    @Override
    public void delete(Long id) {
        Event entity = eventRepository.findById(id).orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND));
        if (entity.getStatus() != EventStatus.CREATED) {
            throw new ConflictException("Only CREATED events can be deleted");
        }
        eventRepository.deleteById(id);
    }

    @Override
    public PaginationResponse<EventResponse> list(Pageable pageable, String sortParam, boolean isPaginated) {
        Page<Event> page = eventRepository.findAll(pageable);
        var responses = page.getContent().stream().map(this::toResponse).toList();

        PaginationResponse.PaginationResponseBuilder<EventResponse> builder = PaginationResponse
                .<EventResponse>builder()
                .isPaginated(isPaginated)
                .content(responses)
                .page(PaginationResponse.PageMeta.builder()
                        .number(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build());

        if (sortParam != null && !sortParam.isBlank()) {
            String[] parts = sortParam.split(",");
            String property = parts[0];
            String direction = parts.length > 1 ? parts[1] : "asc";
            builder.sort(PaginationResponse.SortMeta.builder().property(property).direction(direction).build());
        }

        return builder.build();
    }

    private void ensureUpdatable(Event entity) {
        if (entity.getStatus() == EventStatus.LIVE) {
            throw new ConflictException("Updates are restricted when event is LIVE");
        }
        if (entity.getStatus() == EventStatus.CLOSED || entity.getStatus() == EventStatus.CANCELLED) {
            throw new BadRequestException("Updates are not allowed for CLOSED or CANCELLED events");
        }
    }

    private void validateTransition(EventStatus from, EventStatus to) {
        if (Objects.equals(from, to)) {
            throw new BadRequestException("Event is already in status: " + to);
        }
        switch (from) {
            case CREATED -> {
                if (!(to == EventStatus.LIVE || to == EventStatus.CANCELLED)) {
                    throw new BadRequestException("Invalid transition from CREATED to " + to);
                }
            }
            case LIVE -> {
                if (!(to == EventStatus.CLOSED || to == EventStatus.CANCELLED)) {
                    throw new BadRequestException("Invalid transition from LIVE to " + to);
                }
            }
            default -> throw new BadRequestException("No transitions allowed from " + from);
        }
    }

    private EventResponse toResponse(Event entity) {
        String refId = referenceIdFormatter.format("EVT", entity.getId(), 6);
        return EventResponse.builder()
                .id(entity.getId())
                .refId(refId)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}