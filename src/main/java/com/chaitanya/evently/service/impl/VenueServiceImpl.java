package com.chaitanya.evently.service.impl;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.PaginationResponse.Links;
import com.chaitanya.evently.dto.PaginationResponse.PageMeta;
import com.chaitanya.evently.dto.PaginationResponse.SortMeta;
import com.chaitanya.evently.dto.venue.VenueRequest;
import com.chaitanya.evently.dto.venue.VenueResponse;
import com.chaitanya.evently.exception.types.ConflictException;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.chaitanya.evently.model.Venue;
import com.chaitanya.evently.repository.VenueRepository;
import com.chaitanya.evently.service.VenueService;
import com.chaitanya.evently.util.ReferenceIdFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final ReferenceIdFormatter referenceIdFormatter;

    @Override
    @Transactional
    public VenueResponse create(VenueRequest request) {
        if (venueRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Venue with name '" + request.getName() + "' already exists");
        }
        Venue venue = new Venue();
        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setCapacity(0);
        Venue saved = venueRepository.save(venue);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public VenueResponse update(Long id, VenueRequest request) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venue with id=" + id + " not found"));
        if (!venue.getName().equalsIgnoreCase(request.getName()) &&
                venueRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Venue with name '" + request.getName() + "' already exists");
        }
        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        Venue saved = venueRepository.save(venue);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VenueResponse get(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venue with id=" + id + " not found"));
        return toResponse(venue);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!venueRepository.existsById(id)) {
            throw new NotFoundException("Venue with id=" + id + " not found");
        }
        venueRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<VenueResponse> list(Pageable pageable, String sort, boolean forcePaginated) {
        Page<Venue> page = venueRepository.findAll(pageable);
        List<VenueResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .toList();

        boolean isPaginated = forcePaginated || page.getTotalPages() > 1;
        PageMeta pageMeta = PageMeta.builder()
                .number(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        List<SortMeta.SortField> sortFields = pageable.getSort().stream()
                .map(order -> SortMeta.SortField.builder()
                        .property(order.getProperty())
                        .direction(order.getDirection().name().toLowerCase())
                        .build())
                .collect(Collectors.toList());

        SortMeta sortMeta = SortMeta.builder()
                .fields(sortFields)
                .build();

        final String BASE = "/api/v1/venues?page=";
        final String SIZE = "&size=";
        Links links = Links.builder()
                .self(BASE + page.getNumber() + SIZE + page.getSize())
                .first(BASE + 0 + SIZE + page.getSize())
                .last(BASE + Math.max(page.getTotalPages() - 1, 0) + SIZE + page.getSize())
                .next(page.hasNext() ? BASE + (page.getNumber() + 1) + SIZE + page.getSize() : null)
                .prev(page.hasPrevious() ? BASE + (page.getNumber() - 1) + SIZE + page.getSize() : null)
                .build();

        PaginationResponse.PaginationResponseBuilder<VenueResponse> builder = PaginationResponse
                .<VenueResponse>builder()
                .isPaginated(isPaginated)
                .content(content)
                .page(pageMeta)
                .sort(sortMeta);

        // Only add links for paginated responses
        if (isPaginated) {
            builder.links(links);
        }

        return builder.build();
    }

    private VenueResponse toResponse(Venue venue) {
        return VenueResponse.builder()
                .id(venue.getId())
                .refId(venue.getId() != null ? referenceIdFormatter.format(VenueResponse.REF_PREFIX, venue.getId(), 5)
                        : null)
                .name(venue.getName())
                .address(venue.getAddress())
                .capacity(venue.getCapacity())
                .build();
    }
}