package com.chaitanya.evently.service;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.EventRequest;
import com.chaitanya.evently.dto.event.EventStatusUpdateRequest;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.exception.types.BadRequestException;
import com.chaitanya.evently.exception.types.ConflictException;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.chaitanya.evently.model.Event;
import com.chaitanya.evently.model.status.EventStatus;
import com.chaitanya.evently.model.status.ShowStatus;
import com.chaitanya.evently.repository.EventRepository;
import com.chaitanya.evently.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final ShowRepository showRepository;

    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Event getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + id));
        return event;
    }

    @Transactional(readOnly = true)
    public Event getEventByIdForUser(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + id));

        // Check if event is LIVE or CLOSED (not CREATED)
        if (event.getStatus() == EventStatus.CREATED) {
            throw new NotFoundException("Event not found with id: " + id);
        }

        return event;
    }

    @Transactional(readOnly = true)
    public Event getEventByTitleForUser(String title) {
        Event event = eventRepository.findLiveAndClosedEventByTitle(title)
                .orElseThrow(() -> new NotFoundException("Event not found with title: " + title));

        return event;
    }

    @Transactional(readOnly = true)
    public Event getEventByTitleForAdmin(String title) {
        Event event = eventRepository.findByTitle(title)
                .orElseThrow(() -> new NotFoundException("Event not found with title: " + title));

        return event;
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Event> getEvents(String category, PaginationRequest paginationRequest,
            String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Event> eventPage;

        if (category != null && !category.trim().isEmpty()) {
            eventPage = eventRepository.findByCategory(category, pageable);
        } else {
            eventPage = eventRepository.findAll(pageable);
        }

        return PaginationResponse.fromPage(eventPage, baseUrl, category);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Event> getEventsForUser(String category, PaginationRequest paginationRequest,
            String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Event> eventPage;

        if (category != null && !category.trim().isEmpty()) {
            eventPage = eventRepository.findLiveAndClosedEventsByCategory(category, pageable);
        } else {
            eventPage = eventRepository.findLiveAndClosedEvents(pageable);
        }

        return PaginationResponse.fromPage(eventPage, baseUrl, category);
    }

    @Transactional
    public Event createEvent(EventRequest request) {
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

        return savedEvent;
    }

    @Transactional
    public Event updateEvent(Long id, EventRequest request) {
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

        return updatedEvent;
    }

    @Transactional
    public Event updateEventStatus(Long id, EventStatusUpdateRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + id));

        EventStatus currentStatus = event.getStatus();
        EventStatus newStatus = request.getStatus();

        // Validate state transition
        validateStateTransition(currentStatus, newStatus);

        // If attempting to close the event, ensure there are no LIVE shows
        if (newStatus == EventStatus.CLOSED) {
            var liveShows = showRepository.findByEventIdAndStatus(id,
                    ShowStatus.LIVE);

            if (!(liveShows.isEmpty())) {
                throw new BadRequestException("There are still LIVE shows for this event. Cancel those shows first.");
            }
        }

        event.setStatus(newStatus);
        Event updatedEvent = eventRepository.save(event);

        log.info("Updated event status from {} to {} for event with id: {}",
                currentStatus, newStatus, updatedEvent.getId());

        return updatedEvent;
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Event> getEventsForAdmin(String category, PaginationRequest paginationRequest,
            String baseUrl) {
        // Admin can see all events regardless of status
        return getEvents(category, paginationRequest, baseUrl);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + id));

        // Delete all shows first
        showRepository.deleteByEventId(id);

        eventRepository.delete(event);
        log.info("Deleted event with id: {} and title: {} along with all associated shows", event.getId(),
                event.getTitle());
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

}
