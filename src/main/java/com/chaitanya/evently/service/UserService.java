package com.chaitanya.evently.service;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.exception.types.BadRequestException;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.chaitanya.evently.model.User;
import com.chaitanya.evently.repository.UserRepository;
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
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return user;
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        return user;
    }

    @Transactional(readOnly = true)
    public User getUserByName(String fullName) {
        User user = userRepository.findByFullName(fullName)
                .orElseThrow(() -> new NotFoundException("User not found with name: " + fullName));
        return user;
    }

    @Transactional(readOnly = true)
    public PaginationResponse<User> getUsers(PaginationRequest paginationRequest, String baseUrl) {
        Pageable pageable = createPageable(paginationRequest);
        Page<User> userPage = userRepository.findAll(pageable);

        return PaginationResponse.fromPage(userPage, baseUrl);
    }

    // Private Helper Methods

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
