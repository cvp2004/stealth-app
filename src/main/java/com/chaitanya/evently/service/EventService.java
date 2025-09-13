package com.chaitanya.evently.service;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.EventRequest;
import com.chaitanya.evently.dto.event.EventResponse;
import com.chaitanya.evently.dto.event.EventStatusUpdateRequest;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.exception.types.BadRequestException;
import com.chaitanya.evently.exception.types.ConflictException;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.chaitanya.evently.model.Event;
import com.chaitanya.evently.model.status.EventStatus;
import com.chaitanya.evently.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + id));
        return mapToEventResponse(event);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<EventResponse> getEvents(String category, PaginationRequest paginationRequest,
            String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Event> eventPage;

        if (category != null && !category.trim().isEmpty()) {
            eventPage = eventRepository.findByCategory(category, pageable);
        } else {
            eventPage = eventRepository.findAll(pageable);
        }

        Page<EventResponse> eventResponsePage = eventPage.map(this::mapToEventResponse);

        return PaginationResponse.fromPage(eventResponsePage, baseUrl, category);
    }

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        if (eventRepository.existsByTitle(request.getTitle())) {
            throw new ConflictException("Event with title '" + request.getTitle() + "' already exists");
        }

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setCategory(request.getCategory());
        event.setStatus(EventStatus.CREATED);

        Event savedEvent = eventRepository.save(event);
        log.info("Created event with id: {} and title: {}", savedEvent.getId(), savedEvent.getTitle());

        return mapToEventResponse(savedEvent);
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + id));

        // Check if title is being changed and if new title already exists
        if (!event.getTitle().equals(request.getTitle()) && eventRepository.existsByTitle(request.getTitle())) {
            throw new ConflictException("Event with title '" + request.getTitle() + "' already exists");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setCategory(request.getCategory());

        Event updatedEvent = eventRepository.save(event);
        log.info("Updated event with id: {} and title: {}", updatedEvent.getId(), updatedEvent.getTitle());

        return mapToEventResponse(updatedEvent);
    }

    @Transactional
    public EventResponse updateEventStatus(Long id, EventStatusUpdateRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + id));

        EventStatus currentStatus = event.getStatus();
        EventStatus newStatus = request.getStatus();

        // Validate state transition
        validateStateTransition(currentStatus, newStatus);

        event.setStatus(newStatus);
        Event updatedEvent = eventRepository.save(event);
        log.info("Updated event status from {} to {} for event with id: {}",
                currentStatus, newStatus, updatedEvent.getId());

        return mapToEventResponse(updatedEvent);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<EventResponse> getEventsForAdmin(String category, PaginationRequest paginationRequest,
            String baseUrl) {
        // Admin can see all events regardless of status
        return getEvents(category, paginationRequest, baseUrl);
    }

    private void validateStateTransition(EventStatus currentStatus, EventStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // No change needed
        }

        switch (currentStatus) {
            case CREATED:
                if (newStatus != EventStatus.LIVE) {
                    throw new BadRequestException("Event can only transition from CREATED to LIVE");
                }
                break;
            case LIVE:
                if (newStatus != EventStatus.CLOSED) {
                    throw new BadRequestException("Event can only transition from LIVE to CLOSED");
                }
                break;
            case CLOSED:
                throw new BadRequestException("Event is already CLOSED and cannot transition to any other status");
            default:
                throw new BadRequestException("Invalid current status: " + currentStatus);
        }
    }

    private Pageable createPageable(PaginationRequest paginationRequest) {
        Sort.Direction direction = "desc".equalsIgnoreCase(paginationRequest.getDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // Only allow sorting by createdAt
        if (!"createdAt".equals(paginationRequest.getSort())) {
            throw new BadRequestException("Only 'createdAt' is allowed as sort field");
        }

        Sort sort = Sort.by(direction, "createdAt");
        return PageRequest.of(paginationRequest.getPage(), paginationRequest.getSize(), sort);
    }

    private EventResponse mapToEventResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(event.getCategory())
                .status(event.getStatus())
                .build();
    }
}
