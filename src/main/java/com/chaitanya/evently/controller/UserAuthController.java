package com.chaitanya.evently.controller;

import com.chaitanya.evently.dto.auth.SigninResponse;
import com.chaitanya.evently.dto.auth.SigninRequest;
import com.chaitanya.evently.dto.auth.SignupRequest;
import com.chaitanya.evently.dto.auth.SignupResponse;
import com.chaitanya.evently.model.User;
import com.chaitanya.evently.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class UserAuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request received for email: {}", request.getEmail());
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<SigninResponse> signin(@Valid @RequestBody SigninRequest request) {
        log.info("Signin request received for email: {}", request.getEmail());
        SigninResponse response = authService.signin(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        log.info("Get user request for ID: {}", userId);
        User response = authService.getUserById(userId);
        return ResponseEntity.ok(response);
    }
}
