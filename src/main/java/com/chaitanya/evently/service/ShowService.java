package com.chaitanya.evently.service;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.show.ShowFilterRequest;
import com.chaitanya.evently.dto.show.ShowRequest;
import com.chaitanya.evently.dto.show.ShowResponse;
import com.chaitanya.evently.dto.show.ShowStatusUpdateRequest;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.exception.types.BadRequestException;
import com.chaitanya.evently.exception.types.ConflictException;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.chaitanya.evently.model.Event;
import com.chaitanya.evently.model.Show;
import com.chaitanya.evently.model.Venue;
import com.chaitanya.evently.model.status.ShowStatus;
import com.chaitanya.evently.repository.EventRepository;
import com.chaitanya.evently.repository.ShowRepository;
import com.chaitanya.evently.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowService {

    private final ShowRepository showRepository;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public ShowResponse getShowById(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Show not found with id: " + id));
        return mapToShowResponse(show);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<ShowResponse> getShows(ShowFilterRequest filterRequest,
            PaginationRequest paginationRequest, String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Show> showPage = applyFilters(filterRequest, pageable);

        Page<ShowResponse> showResponsePage = showPage.map(this::mapToShowResponse);

        return PaginationResponse.fromPage(showResponsePage, baseUrl);
    }

    @Transactional
    public ShowResponse createShow(ShowRequest request) {
        // Validate venue exists
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new NotFoundException("Venue not found with id: " + request.getVenueId()));

        // Validate event exists
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + request.getEventId()));

        // Check for overlapping shows at the same venue
        if (hasOverlappingShows(request.getVenueId(), request.getStartTimestamp(), request.getDurationMinutes())) {
            throw new ConflictException("Show overlaps with existing show at the same venue");
        }

        Show show = new Show();
        show.setVenue(venue);
        show.setEvent(event);
        show.setStartTimestamp(request.getStartTimestamp());
        show.setDurationMinutes(request.getDurationMinutes());
        show.setStatus(ShowStatus.LIVE);

        Show savedShow = showRepository.save(show);
        log.info("Created show with id: {} for event: {} at venue: {}",
                savedShow.getId(), event.getTitle(), venue.getName());

        return mapToShowResponse(savedShow);
    }

    @Transactional
    public ShowResponse updateShow(Long id, ShowRequest request) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Show not found with id: " + id));

        // Validate venue exists
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new NotFoundException("Venue not found with id: " + request.getVenueId()));

        // Validate event exists
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + request.getEventId()));

        // Check for overlapping shows at the same venue (excluding current show)
        if (hasOverlappingShowsExcludingCurrent(request.getVenueId(), request.getStartTimestamp(),
                request.getDurationMinutes(), id)) {
            throw new ConflictException("Show overlaps with existing show at the same venue");
        }

        show.setVenue(venue);
        show.setEvent(event);
        show.setStartTimestamp(request.getStartTimestamp());
        show.setDurationMinutes(request.getDurationMinutes());

        Show updatedShow = showRepository.save(show);
        log.info("Updated show with id: {} for event: {} at venue: {}",
                updatedShow.getId(), event.getTitle(), venue.getName());

        return mapToShowResponse(updatedShow);
    }

    @Transactional
    public ShowResponse updateShowStatus(Long id, ShowStatusUpdateRequest request) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Show not found with id: " + id));

        ShowStatus currentStatus = show.getStatus();
        ShowStatus newStatus = request.getStatus();

        // Validate state transition
        validateShowStateTransition(currentStatus, newStatus);

        show.setStatus(newStatus);
        Show updatedShow = showRepository.save(show);
        log.info("Updated show status from {} to {} for show with id: {}",
                currentStatus, newStatus, updatedShow.getId());

        return mapToShowResponse(updatedShow);
    }

    @Transactional
    public void deleteShow(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Show not found with id: " + id));

        showRepository.delete(show);
        log.info("Deleted show with id: {} for event: {} at venue: {}",
                id, show.getEvent().getTitle(), show.getVenue().getName());
    }

    @Transactional
    public void deleteShowsByVenueId(Long venueId) {
        showRepository.deleteByVenueId(venueId);
        log.info("Deleted all shows for venue with id: {}", venueId);
    }

    @Transactional
    public void deleteShowsByEventId(Long eventId) {
        showRepository.deleteByEventId(eventId);
        log.info("Deleted all shows for event with id: {}", eventId);
    }

    @Transactional
    public void cancelLiveShowsForEvent(Long eventId) {
        List<Show> liveShows = showRepository.findByEventIdAndStatus(eventId, ShowStatus.LIVE);
        for (Show show : liveShows) {
            show.setStatus(ShowStatus.CANCELLED);
        }
        showRepository.saveAll(liveShows);
        log.info("Cancelled {} live shows for event with id: {}", liveShows.size(), eventId);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<ShowResponse> getShowsForAdmin(ShowFilterRequest filterRequest,
            PaginationRequest paginationRequest, String baseUrl) {
        // Admin can see all shows regardless of status
        return getShows(filterRequest, paginationRequest, baseUrl);
    }

    private Page<Show> applyFilters(ShowFilterRequest filterRequest, Pageable pageable) {
        if (filterRequest == null) {
            return showRepository.findAll(pageable);
        }

        Long venueId = filterRequest.getVenueId();
        Long eventId = filterRequest.getEventId();
        Instant date = filterRequest.getDate();
        Instant fromDate = filterRequest.getFromDate();
        Instant toDate = filterRequest.getToDate();

        // Apply filters based on what's provided
        if (venueId != null && eventId != null) {
            if (fromDate != null && toDate != null) {
                return showRepository.findByVenueIdAndEventIdAndDateRange(venueId, eventId, fromDate, toDate, pageable);
            } else if (date != null) {
                LocalDate localDate = LocalDate.ofInstant(date, ZoneOffset.UTC);
                return showRepository.findByVenueIdAndEventIdAndDateRange(venueId, eventId,
                        localDate.atStartOfDay().toInstant(ZoneOffset.UTC),
                        localDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC), pageable);
            } else {
                return showRepository.findByVenueIdAndEventId(venueId, eventId, pageable);
            }
        } else if (venueId != null) {
            if (fromDate != null && toDate != null) {
                return showRepository.findByVenueIdAndDateRange(venueId, fromDate, toDate, pageable);
            } else if (date != null) {
                LocalDate localDate = LocalDate.ofInstant(date, ZoneOffset.UTC);
                return showRepository.findByVenueIdAndDate(venueId,
                        localDate.atStartOfDay().toInstant(ZoneOffset.UTC), pageable);
            } else {
                return showRepository.findByVenueId(venueId, pageable);
            }
        } else if (eventId != null) {
            if (fromDate != null && toDate != null) {
                return showRepository.findByEventIdAndDateRange(eventId, fromDate, toDate, pageable);
            } else if (date != null) {
                LocalDate localDate = LocalDate.ofInstant(date, ZoneOffset.UTC);
                return showRepository.findByEventIdAndDate(eventId,
                        localDate.atStartOfDay().toInstant(ZoneOffset.UTC), pageable);
            } else {
                return showRepository.findByEventId(eventId, pageable);
            }
        } else if (fromDate != null && toDate != null) {
            return showRepository.findByDateRange(fromDate, toDate, pageable);
        } else if (date != null) {
            LocalDate localDate = LocalDate.ofInstant(date, ZoneOffset.UTC);
            return showRepository.findByDate(localDate.atStartOfDay().toInstant(ZoneOffset.UTC), pageable);
        } else {
            return showRepository.findAll(pageable);
        }
    }

    private boolean hasOverlappingShows(Long venueId, Instant startTimestamp, Integer durationMinutes) {
        Instant endTimestamp = startTimestamp.plusSeconds(durationMinutes * 60L);

        // Check for overlapping shows at the same venue
        List<Show> existingShows = showRepository.findByVenueId(venueId, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();

        for (Show existingShow : existingShows) {
            Instant existingStart = existingShow.getStartTimestamp();
            Instant existingEnd = existingStart.plusSeconds(existingShow.getDurationMinutes() * 60L);

            // Check if shows overlap
            if (startTimestamp.isBefore(existingEnd) && endTimestamp.isAfter(existingStart)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasOverlappingShowsExcludingCurrent(Long venueId, Instant startTimestamp, Integer durationMinutes,
            Long currentShowId) {
        Instant endTimestamp = startTimestamp.plusSeconds(durationMinutes * 60L);

        // Check for overlapping shows at the same venue (excluding current show)
        List<Show> existingShows = showRepository.findByVenueId(venueId, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();

        for (Show existingShow : existingShows) {
            if (existingShow.getId().equals(currentShowId)) {
                continue; // Skip current show
            }

            Instant existingStart = existingShow.getStartTimestamp();
            Instant existingEnd = existingStart.plusSeconds(existingShow.getDurationMinutes() * 60L);

            // Check if shows overlap
            if (startTimestamp.isBefore(existingEnd) && endTimestamp.isAfter(existingStart)) {
                return true;
            }
        }

        return false;
    }

    private void validateShowStateTransition(ShowStatus currentStatus, ShowStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // No change needed
        }

        switch (currentStatus) {
            case LIVE:
                if (newStatus != ShowStatus.CLOSED && newStatus != ShowStatus.CANCELLED) {
                    throw new BadRequestException("Show can only transition from LIVE to CLOSED or CANCELLED");
                }
                break;
            case CLOSED:
                throw new BadRequestException("Show is already CLOSED and cannot transition to any other status");
            case CANCELLED:
                throw new BadRequestException("Show is already CANCELLED and cannot transition to any other status");
            default:
                throw new BadRequestException("Invalid current status: " + currentStatus);
        }
    }

    private Pageable createPageable(PaginationRequest paginationRequest) {
        Sort.Direction direction = "desc".equalsIgnoreCase(paginationRequest.getDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // Only allow sorting by startTimestamp
        if (!"startTimestamp".equals(paginationRequest.getSort())) {
            throw new BadRequestException("Only 'startTimestamp' is allowed as sort field");
        }

        Sort sort = Sort.by(direction, "startTimestamp");
        return PageRequest.of(paginationRequest.getPage(), paginationRequest.getSize(), sort);
    }

    private ShowResponse mapToShowResponse(Show show) {
        return ShowResponse.builder()
                .id(show.getId())
                .venueId(show.getVenue().getId())
                .venueName(show.getVenue().getName())
                .eventId(show.getEvent().getId())
                .eventTitle(show.getEvent().getTitle())
                .eventDescription(show.getEvent().getDescription())
                .eventCategory(show.getEvent().getCategory())
                .startTimestamp(show.getStartTimestamp())
                .durationMinutes(show.getDurationMinutes())
                .status(show.getStatus())
                .createdAt(show.getCreatedAt())
                .build();
    }
}
