package com.chaitanya.evently.controller.admin;

import com.chaitanya.evently.dto.PaginationResponse;
import com.chaitanya.evently.dto.event.PaginationRequest;
import com.chaitanya.evently.model.User;
import com.chaitanya.evently.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/user")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        log.info("Admin requested user with id: {}", id);
        User user = userService.getUserById(id);
        return ResponseEntity.ok(toUserResponse(user));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Map<String, Object>> getUserByEmail(@PathVariable String email) {
        log.info("Admin requested user with email: {}", email);
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(toUserResponse(user));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Map<String, Object>> getUserByName(@PathVariable String name) {
        log.info("Admin requested user with name: {}", name);
        User user = userService.getUserByName(name);
        return ResponseEntity.ok(toUserResponse(user));
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getUsers(
            @Valid @RequestBody PaginationRequest paginationRequest,
            HttpServletRequest request) {

        log.info("Admin requested all users with pagination: page={}, size={}",
                paginationRequest.getPage(), paginationRequest.getSize());

        String baseUrl = request.getRequestURL().toString();
        PaginationResponse<User> users = userService.getUsers(paginationRequest, baseUrl);
        return ResponseEntity.ok(toPaginationResponseMap(users));
    }

    // Private Mapper Methods

    private Map<String, Object> toUserResponse(User user) {
        return Map.of(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail());
    }

    private Map<String, Object> toPaginationResponseMap(PaginationResponse<User> response) {
        List<Map<String, Object>> content = response.getContent() == null ? List.of()
                : response.getContent().stream()
                        .map(this::toUserResponse)
                        .collect(Collectors.toList());

        Map<String, Object> page = response.getPage() == null ? Map.of()
                : Map.of(
                        "number", response.getPage().getNumber(),
                        "size", response.getPage().getSize(),
                        "totalElements", response.getPage().getTotalElements(),
                        "totalPages", response.getPage().getTotalPages());

        List<Map<String, Object>> sortFields = (response.getSort() == null || response.getSort().getFields() == null)
                ? List.of()
                : response.getSort().getFields().stream()
                        .map(f -> Map.<String, Object>of(
                                "property", f.getProperty(),
                                "direction", f.getDirection()))
                        .collect(Collectors.toList());

        Map<String, Object> sort = Map.of("fields", sortFields);

        Map<String, Object> links = response.getLinks() == null ? Map.of()
                : Map.of(
                        "self", response.getLinks().getSelf(),
                        "first", response.getLinks().getFirst(),
                        "last", response.getLinks().getLast(),
                        "next", response.getLinks().getNext(),
                        "prev", response.getLinks().getPrev());

        return Map.of(
                "isPaginated", response.isPaginated(),
                "content", content,
                "page", page,
                "sort", sort,
                "links", links);
    }
}