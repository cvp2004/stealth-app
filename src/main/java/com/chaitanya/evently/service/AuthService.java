package com.chaitanya.evently.service;

import com.chaitanya.evently.dto.auth.SigninResponse;
import com.chaitanya.evently.dto.auth.SigninRequest;
import com.chaitanya.evently.dto.auth.SignupRequest;
import com.chaitanya.evently.dto.auth.SignupResponse;
import com.chaitanya.evently.exception.types.BadRequestException;
import com.chaitanya.evently.exception.types.ConflictException;
import com.chaitanya.evently.exception.types.UnauthorizedException;
import com.chaitanya.evently.model.User;
import com.chaitanya.evently.repository.UserRepository;
import com.chaitanya.evently.util.SimplePasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final SimplePasswordEncoder passwordEncoder = new SimplePasswordEncoder();

    public SignupResponse signup(SignupRequest request) {
        log.info("User signup request for email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("User with email " + request.getEmail() + " already exists");
        }

        // Create new user
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return SignupResponse.builder()
                .message("User registered successfully")
                .build();
    }

    public SigninResponse signin(SigninRequest request) {
        log.info("User signin request for email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        log.info("User signed in successfully with ID: {}", user.getId());

        return SigninResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .message("User signed in successfully")
                .build();
    }

    public User getUserById(Long userId) {
        log.info("Getting user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with ID: " + userId));

        return user;
    }
}
