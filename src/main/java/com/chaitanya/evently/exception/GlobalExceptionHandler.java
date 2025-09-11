package com.chaitanya.evently.exception;

import com.chaitanya.evently.exception.types.BadRequestException;
import com.chaitanya.evently.exception.types.ConflictException;
import com.chaitanya.evently.exception.types.NotFoundException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        List<ErrorResponse.FieldError> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", firstMessage(ex), path(request), details);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), path(request), null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), path(request), null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), path(request), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), path(request), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "", path(request), null);
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
        return ResponseEntity.status(status).body(body);
    }

    private String path(WebRequest request) {
        if (request instanceof ServletWebRequest swr) {
            return swr.getRequest().getRequestURI();
        }
        return null;
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
