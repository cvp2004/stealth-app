package com.chaitanya.evently.service;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.chaitanya.evently.model.Ticket;
import com.chaitanya.evently.repository.TicketRepository;
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
public class TicketService {

    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public Ticket getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found with id: " + id));
        return ticket;
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Ticket> getTicketsByUserId(Long userId, PaginationRequest paginationRequest,
            String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Ticket> ticketPage = ticketRepository.findByUserId(userId, pageable);

        return PaginationResponse.fromPage(ticketPage, baseUrl);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Ticket> getAllTickets(PaginationRequest paginationRequest, String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Ticket> ticketPage = ticketRepository.findAll(pageable);

        return PaginationResponse.fromPage(ticketPage, baseUrl);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Ticket> getTicketsByBookingId(Long bookingId, PaginationRequest paginationRequest,
            String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        // We only have List<Ticket> by bookingId; implement pageable manually
        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        int page = paginationRequest.getPage();
        int size = paginationRequest.getSize();
        int fromIndex = Math.min(page * size, tickets.size());
        int toIndex = Math.min(fromIndex + size, tickets.size());
        List<Ticket> slice = tickets.subList(fromIndex, toIndex);

        Page<Ticket> ticketPage = new org.springframework.data.domain.PageImpl<>(slice, pageable, tickets.size());
        return PaginationResponse.fromPage(ticketPage, baseUrl);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<Ticket> getTicketsByUserIdAndShowId(Long userId, Long showId,
            PaginationRequest paginationRequest, String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<Ticket> ticketPage = ticketRepository.findByUserIdAndShowId(userId, showId, pageable);

        return PaginationResponse.fromPage(ticketPage, baseUrl);
    }

    private Pageable createPageable(PaginationRequest paginationRequest) {
        Sort.Direction direction = "desc".equalsIgnoreCase(paginationRequest.getDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // Only allow sorting by createdAt
        if (!"createdAt".equals(paginationRequest.getSort())) {
            throw new IllegalArgumentException("Only 'createdAt' is allowed as sort field");
        }

        Sort sort = Sort.by(direction, "createdAt");
        return PageRequest.of(paginationRequest.getPage(), paginationRequest.getSize(), sort);
    }

}
