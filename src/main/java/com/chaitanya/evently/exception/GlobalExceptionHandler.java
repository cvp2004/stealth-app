package com.chaitanya.evently.exception;

import com.chaitanya.evently.dto.ErrorResponse;
import com.chaitanya.evently.exception.types.BadRequestException;
import com.chaitanya.evently.exception.types.ConflictException;
import com.chaitanya.evently.exception.types.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        String requestPath = path(request);
        String firstErrorMessage = firstMessage(ex);

        log.warn("Validation failed for request: {} - Error: {}", requestPath, firstErrorMessage);
        log.debug("Validation details: {}", ex.getBindingResult().getFieldErrors());

        List<ErrorResponse.FieldError> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", firstErrorMessage, requestPath, details);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, WebRequest request) {
        String requestPath = path(request);
        log.warn("Resource not found for request: {} - Error: {}", requestPath, ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), requestPath, null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, WebRequest request) {
        String requestPath = path(request);
        log.warn("Conflict detected for request: {} - Error: {}", requestPath, ex.getMessage());
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), requestPath, null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, WebRequest request) {
        String requestPath = path(request);
        log.warn("Bad request for: {} - Error: {}", requestPath, ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), requestPath, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        String requestPath = path(request);
        log.warn("Illegal argument for request: {} - Error: {}", requestPath, ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), requestPath, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParseError(HttpMessageNotReadableException ex, WebRequest request) {
        String requestPath = path(request);
        String message = "Invalid JSON format";

        if (ex.getCause() instanceof JsonMappingException) {
            message = "JSON mapping error: " + ex.getCause().getMessage();
            log.warn("JSON mapping error for request: {} - Error: {}", requestPath, ex.getCause().getMessage());
        } else if (ex.getCause() instanceof JsonProcessingException) {
            message = "JSON processing error: " + ex.getCause().getMessage();
            log.warn("JSON processing error for request: {} - Error: {}", requestPath, ex.getCause().getMessage());
        } else {
            log.warn("JSON parse error for request: {} - Error: {}", requestPath, ex.getMessage());
        }

        // Log the received body for debugging
        if (request instanceof ServletWebRequest swr) {
            try {
                String body = new String(swr.getRequest().getInputStream().readAllBytes());
                log.debug("Received request body for {}: {}", requestPath, body);
            } catch (IOException e) {
                log.warn("Could not read request body for {}: {}", requestPath, e.getMessage());
            }
        }

        return build(HttpStatus.BAD_REQUEST, "Bad Request", message, requestPath, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        String requestPath = path(request);
        log.error("Unexpected error occurred for request: {} - Error: {}", requestPath, ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred",
                requestPath, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message, String path,
            List<ErrorResponse.FieldError> details) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .details(details)
                .build();

        log.debug("Returning error response: Status={}, Path={}, Message={}", status.value(), path, message);
        return ResponseEntity.status(status).body(body);
    }

    private String path(WebRequest request) {
        if (request instanceof ServletWebRequest swr) {
            String uri = swr.getRequest().getRequestURI();
            String method = swr.getRequest().getMethod();
            return method + " " + uri;
        }
        return "Unknown";
    }

    private String firstMessage(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(o -> o.getDefaultMessage())
                .orElse("Validation failed");
    }

    private ErrorResponse.FieldError toFieldError(FieldError fe) {
        return ErrorResponse.FieldError.builder()
                .field(fe.getField())
                .message(fe.getDefaultMessage())
                .build();
    }
}
