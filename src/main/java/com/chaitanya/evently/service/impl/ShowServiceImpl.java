package com.chaitanya.evently.service.impl;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.show.request.ShowRequest;
import com.chaitanya.evently.dto.show.response.ShowResponse;
import com.chaitanya.evently.exception.types.BadRequestException;
import com.chaitanya.evently.exception.types.ConflictException;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.chaitanya.evently.model.Event;
import com.chaitanya.evently.model.Show;
import com.chaitanya.evently.model.Venue;
import com.chaitanya.evently.repository.EventRepository;
import com.chaitanya.evently.repository.ShowRepository;
import com.chaitanya.evently.repository.VenueRepository;
import com.chaitanya.evently.service.ShowService;
import com.chaitanya.evently.util.ReferenceIdFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowServiceImpl implements ShowService {

    private final ShowRepository showRepository;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final ReferenceIdFormatter referenceIdFormatter;

    private static final String SHOW_NOT_FOUND = "Show not found";
    private static final String VENUE_NOT_FOUND = "Venue not found";
    private static final String EVENT_NOT_FOUND = "Event not found";

    @Override
    @Transactional
    public ShowResponse create(ShowRequest request) {
        // Validate venue exists
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new NotFoundException(VENUE_NOT_FOUND));

        // Validate event exists (now mandatory)
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND));

        // Calculate end time for conflict checking
        Instant endTime = request.getStartTimestamp().plusSeconds(request.getDurationMinutes() * 60L);

        // Check for time conflicts
        if (showRepository.hasTimeConflict(request.getVenueId(), request.getStartTimestamp(),
                endTime, null)) {
            throw new ConflictException("Show time conflicts with existing show at this venue");
        }

        Show entity = new Show();
        entity.setVenue(venue);
        entity.setEvent(event);
        entity.setTitle(event.getTitle()); // Inherit title from event
        entity.setDescription(event.getDescription()); // Inherit description from event
        entity.setStartTimestamp(request.getStartTimestamp());
        entity.setDurationMinutes(request.getDurationMinutes());

        Show saved = showRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ShowResponse get(Long id) {
        Show entity = showRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(SHOW_NOT_FOUND));
        return toResponse(entity);
    }

    @Override
    @Transactional
    public ShowResponse update(Long id, ShowRequest request) {
        Show entity = showRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(SHOW_NOT_FOUND));

        // Validate venue exists
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new NotFoundException(VENUE_NOT_FOUND));

        // Validate event exists (now mandatory)
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new NotFoundException(EVENT_NOT_FOUND));

        // Calculate end time for conflict checking
        Instant endTime = request.getStartTimestamp().plusSeconds(request.getDurationMinutes() * 60L);

        // Check for time conflicts (excluding current show)
        if (showRepository.hasTimeConflict(request.getVenueId(), request.getStartTimestamp(),
                endTime, id)) {
            throw new ConflictException("Show time conflicts with existing show at this venue");
        }

        entity.setVenue(venue);
        entity.setEvent(event);
        // Inherit title and description from event
        entity.setTitle(event.getTitle());
        entity.setDescription(event.getDescription());
        entity.setStartTimestamp(request.getStartTimestamp());
        entity.setDurationMinutes(request.getDurationMinutes());

        Show saved = showRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!showRepository.existsById(id)) {
            throw new NotFoundException(SHOW_NOT_FOUND);
        }

        // TODO: Check for existing bookings before deletion
        // This would require checking BookedSeat or Booking entities

        showRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<ShowResponse> list(
            Pageable pageable,
            String sort,
            boolean isPaginated,
            Long venueId,
            Long eventId,
            Instant date,
            Instant fromDate,
            Instant toDate) {
        // Validate date parameters
        validateDateParameters(date, fromDate, toDate);

        // Use the repository method with filters
        Page<Show> page = showRepository.findShowsWithFilters(
                venueId, eventId, date, fromDate, toDate, pageable);

        // Convert to response and build pagination
        List<ShowResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .toList();

        return buildPaginationResponse(page, content, isPaginated, sort);
    }

    private void validateDateParameters(Instant date, Instant fromDate, Instant toDate) {
        if (date != null && (fromDate != null || toDate != null)) {
            throw new BadRequestException("Cannot specify both 'date' and 'fromDate'/'toDate' parameters");
        }

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException("fromDate must be before or equal to toDate");
        }
    }

    private PaginationResponse<ShowResponse> buildPaginationResponse(
            Page<Show> page,
            List<ShowResponse> content,
            boolean isPaginated,
            String sort) {
        PaginationResponse.PaginationResponseBuilder<ShowResponse> builder = PaginationResponse
                .<ShowResponse>builder()
                .isPaginated(isPaginated)
                .content(content)
                .page(PaginationResponse.PageMeta.builder()
                        .number(page.getNumber())
                        .size(page.getSize())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build());

        if (sort != null && !sort.isBlank()) {
            List<PaginationResponse.SortMeta.SortField> sortFields = Arrays.stream(sort.split(";"))
                    .map(field -> {
                        String[] parts = field.trim().split(",");
                        String property = parts[0].trim();
                        String direction = parts.length > 1 ? parts[1].trim() : "asc";
                        return PaginationResponse.SortMeta.SortField.builder()
                                .property(property)
                                .direction(direction)
                                .build();
                    })
                    .collect(Collectors.toList());

            builder.sort(PaginationResponse.SortMeta.builder().fields(sortFields).build());
        }

        // Only add links for paginated responses
        if (isPaginated) {
            final String BASE = "/api/v1/shows?page=";
            final String SIZE = "&size=";
            PaginationResponse.Links links = PaginationResponse.Links.builder()
                    .self(BASE + page.getNumber() + SIZE + page.getSize())
                    .first(BASE + 0 + SIZE + page.getSize())
                    .last(BASE + Math.max(page.getTotalPages() - 1, 0) + SIZE + page.getSize())
                    .next(page.hasNext() ? BASE + (page.getNumber() + 1) + SIZE + page.getSize() : null)
                    .prev(page.hasPrevious() ? BASE + (page.getNumber() - 1) + SIZE + page.getSize() : null)
                    .build();
            builder.links(links);
        }

        return builder.build();
    }

    private ShowResponse toResponse(Show entity) {
        String refId = referenceIdFormatter.format("SHW", entity.getId(), 6);

        ShowResponse.VenueInfo venueInfo = ShowResponse.VenueInfo.builder()
                .id(entity.getVenue().getId())
                .name(entity.getVenue().getName())
                .address(entity.getVenue().getAddress())
                .build();

        ShowResponse.EventInfo eventInfo = null;
        if (entity.getEvent() != null) {
            eventInfo = ShowResponse.EventInfo.builder()
                    .id(entity.getEvent().getId())
                    .title(entity.getEvent().getTitle())
                    .category(entity.getEvent().getCategory())
                    .build();
        }

        // Convert Instant to LocalDate and LocalTime
        java.time.LocalDate date = entity.getStartTimestamp().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        java.time.LocalTime startTime = entity.getStartTimestamp().atZone(java.time.ZoneId.systemDefault())
                .toLocalTime();

        return ShowResponse.builder()
                .id(entity.getId())
                .refId(refId)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .startTimestamp(entity.getStartTimestamp())
                .date(date)
                .startTime(startTime)
                .durationMinutes(entity.getDurationMinutes())
                .venue(venueInfo)
                .event(eventInfo)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
