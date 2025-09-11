# Error Handling Strategy

This document explains the standardized error handling used across the Evently backend.

## Goals

- Consistent response schema for all errors
- Clear, human-readable messages for clients
- Validations handled by Spring Validation (no custom validators)
- Typed business exceptions for service layer clarity

## Error Response Schema

Responses use a single structure for all error types.

```json
{
  "timestamp": "2025-01-01T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/venues",
  "details": [
    {
      "field": "name",
      "message": "name is required"
    }
  ]
}
```

- timestamp: Server time (UTC) when error was generated
- status: HTTP status code
- error: Reason phrase for the status
- message: High-level explanation
- path: Request path
- details: Optional field-level errors (present for validation failures)

## Where it lives (code)

- Model: `exception/ErrorResponse`
- Global handler: `exception/GlobalExceptionHandler`
- Typed exceptions: `exception/types/*`
  - `NotFoundException` → 404
  - `ConflictException` → 409
  - `BadRequestException` → 400

## What is handled

- Bean Validation errors (DTOs, query params)
  - Triggered by Jakarta annotations (e.g., `@NotBlank`, `@Pattern`)
  - Handled as 400 with `details[]`
- Not Found
  - `NotFoundException` (e.g., entity missing)
- Conflict
  - `ConflictException` (e.g., unique constraint / business conflicts)
- Bad Request
  - `BadRequestException` or other client-side mistakes
- Fallbacks
  - `IllegalArgumentException` → 400
  - Any other unhandled error → 500

## Validation

- DTO validation via annotations
  - Example: `VenueRequest` uses `@NotBlank`, `@Size`, `@NotNull`, `@Positive`
- Query param validation via method-level validation
  - Example: `direction` query param on `GET /api/v1/venues`
    - `@Pattern(regexp = "(?i)asc|desc", message = "direction must be 'asc' or 'desc'")`

## Examples

### 400 Validation error

```json
{
  "timestamp": "2025-09-11T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/venues",
  "details": [
    { "field": "capacity", "message": "capacity must be a positive integer" }
  ]
}
```

### 404 Not Found

```json
{
  "timestamp": "2025-09-11T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Venue with id=123 not found",
  "path": "/api/v1/venues/123"
}
```

### 409 Conflict

```json
{
  "timestamp": "2025-09-11T10:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Venue with name 'Wembley Stadium' already exists",
  "path": "/api/v1/venues"
}
```

## How to throw errors (service layer)

Use typed exceptions with descriptive messages.

```java
if (venueRepository.existsByNameIgnoreCase(request.getName())) {
  throw new ConflictException("Venue with name '" + request.getName() + "' already exists");
}

Venue venue = venueRepository.findById(id)
    .orElseThrow(() -> new NotFoundException("Venue with id=" + id + " not found"));
```

## Extending the handler

- Add new exception class in `exception/types`
- Map it inside `GlobalExceptionHandler` with an `@ExceptionHandler`
- Return appropriate HTTP status and message

## Notes

- Keep messages descriptive and user-focused
- Don’t expose sensitive/internal details
- Prefer validation annotations and typed exceptions over manual checks
