# Evently API Specification

## Overview

Evently is a Spring Boot application that provides APIs for managing events, venues, shows, and seat configurations. The API follows RESTful principles and uses JSON for data exchange.

**Base URL:** `http://localhost:8080/api/v1`

## Authentication

Currently, no authentication is implemented in the API endpoints.

## Common Response Formats

### Pagination Response

All list endpoints return paginated responses with the following structure:

```json
{
  "isPaginated": boolean,
  "content": [T],
  "page": {
    "number": int,
    "size": int,
    "totalElements": long,
    "totalPages": int
  },
  "sort": {
    "property": string,
    "direction": string
  },
  "links": {
    "self": string,
    "first": string,
    "last": string,
    "next": string,
    "prev": string
  }
}
```

### Error Response

All error responses follow this structure:

```json
{
  "timestamp": "2024-01-01T00:00:00Z",
  "status": int,
  "error": string,
  "message": string,
  "path": string,
  "details": [
    {
      "field": string,
      "message": string
    }
  ]
}
```

## API Endpoints

### 1. Events Management

#### Base Path: `/api/v1/events`

| Method | Endpoint       | Description                 | Request Body             | Response                                |
| ------ | -------------- | --------------------------- | ------------------------ | --------------------------------------- |
| POST   | `/`            | Create a new event          | EventRequest             | EventResponse (201)                     |
| GET    | `/`            | List all events (paginated) | Query params             | PaginationResponse<EventResponse> (200) |
| GET    | `/{id}`        | Get event by ID             | -                        | EventResponse (200)                     |
| PUT    | `/{id}`        | Update event by ID          | EventRequest             | EventResponse (200)                     |
| DELETE | `/{id}`        | Delete event by ID          | -                        | No Content (204)                        |
| POST   | `/{id}/status` | Change event status         | EventStatusChangeRequest | EventStatusResponse (200)               |
| GET    | `/{id}/status` | Get event status            | -                        | EventStatusResponse (200)               |

#### Query Parameters for List Events

- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 50)
- `sort` (optional): Sort property (default: "id")
- `direction` (optional): Sort direction - "asc" or "desc" (default: "asc")

#### Event Status Values

- `CREATED`
- `LIVE`
- `CLOSED`
- `CANCELLED`

#### EventRequest

```json
{
  "title": "string (required, max 255 chars)",
  "description": "string (optional, max 2000 chars)",
  "category": "string (optional, max 100 chars)"
}
```

#### EventResponse

```json
{
  "id": "long",
  "refId": "string",
  "title": "string",
  "description": "string",
  "category": "string",
  "status": "EventStatus",
  "createdAt": "Instant",
  "updatedAt": "Instant"
}
```

#### EventStatusChangeRequest

```json
{
  "status": "EventStatus (required)"
}
```

#### EventStatusResponse

```json
{
  "id": "long",
  "refId": "string",
  "title": "string",
  "status": "EventStatus"
}
```

### 2. Shows Management

#### Base Path: `/api/v1/shows`

| Method | Endpoint | Description            | Request Body                     | Response                               |
| ------ | -------- | ---------------------- | -------------------------------- | -------------------------------------- |
| POST   | `/`      | Create a new show      | ShowRequest                      | ShowResponse (201)                     |
| GET    | `/`      | List shows (paginated) | Query params + ShowFilterRequest | PaginationResponse<ShowResponse> (200) |
| GET    | `/{id}`  | Get show by ID         | -                                | ShowResponse (200)                     |
| PUT    | `/{id}`  | Update show by ID      | ShowRequest                      | ShowResponse (200)                     |
| DELETE | `/{id}`  | Delete show by ID      | -                                | No Content (204)                       |

#### List Shows (GET /)

The list endpoint uses query parameters for pagination and an optional request body for filtering:

**Query Parameters (Required for pagination):**

- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 50)
- `sort` (optional): Sort property (default: "startTimestamp,asc")

**Request Body (Optional for filtering):**

```json
{
  "venueId": "number (optional)",
  "eventId": "number (optional)",
  "date": "string (optional, ISO format)",
  "fromDate": "string (optional, ISO format)",
  "toDate": "string (optional, ISO format)"
}
```

**Note**:

- Request body is optional - if not provided, no filtering is applied
- If request body is provided, at least one filter field must be set
- `date` and `fromDate`/`toDate` are mutually exclusive
- Query parameters are always used for pagination and sorting

#### ShowRequest

```json
{
  "venueId": "number (required)",
  "eventId": "number (required)",
  "startTimestamp": "string (required, ISO format, future date)",
  "durationMinutes": "number (required, minimum 1)"
}
```

**Note**: Title and description are automatically inherited from the event specified by `eventId`.

#### ShowResponse

```json
{
  "id": "number",
  "refId": "string",
  "title": "string",
  "description": "string",
  "startTimestamp": "string (ISO format)",
  "date": "string (YYYY-MM-DD format)",
  "startTime": "string (HH:MM:SS format)",
  "durationMinutes": "number",
  "venue": {
    "id": "number",
    "name": "string",
    "address": "string"
  },
  "event": {
    "id": "number",
    "title": "string",
    "category": "string"
  },
  "createdAt": "string (ISO format)",
  "updatedAt": "string (ISO format)"
}
```

#### Example API Calls

**Create a show:**

```bash
POST /api/v1/shows
{
  "venueId": 1,
  "eventId": 5,
  "startTimestamp": "2024-12-25T19:00:00Z",
  "durationMinutes": 180
}
```

**Note**: The title and description will be automatically inherited from the event with ID 5.

**List all shows (simple with query params only):**

```bash
GET /api/v1/shows?page=0&size=20&sort=startTimestamp,asc
```

**List shows with filtering (query params + request body):**

```bash
GET /api/v1/shows?page=0&size=20&sort=startTimestamp,asc
Content-Type: application/json

{
  "venueId": 1
}
```

**List shows by date range:**

```bash
GET /api/v1/shows?page=0&size=50
Content-Type: application/json

{
  "fromDate": "2024-12-01T00:00:00Z",
  "toDate": "2024-12-31T23:59:59Z"
}
```

**List shows by venue and event:**

```bash
GET /api/v1/shows?page=0&size=10&sort=startTimestamp,desc
Content-Type: application/json

{
  "venueId": 1,
  "eventId": 5
}
```

**Invalid request (empty request body):**

```bash
GET /api/v1/shows?page=0&size=10
Content-Type: application/json

{}
```

**Response: 400 Bad Request**

```json
{
  "timestamp": "2024-01-01T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "At least one filter field must be provided",
  "path": "/api/v1/shows"
}
```

### 3. Venues Management

#### Base Path: `/api/v1/venues`

| Method | Endpoint | Description                 | Request Body | Response                                |
| ------ | -------- | --------------------------- | ------------ | --------------------------------------- |
| POST   | `/`      | Create a new venue          | VenueRequest | VenueResponse (201)                     |
| GET    | `/`      | List all venues (paginated) | Query params | PaginationResponse<VenueResponse> (200) |
| GET    | `/{id}`  | Get venue by ID             | -            | VenueResponse (200)                     |
| PUT    | `/{id}`  | Update venue by ID          | VenueRequest | VenueResponse (200)                     |
| DELETE | `/{id}`  | Delete venue by ID          | -            | No Content (204)                        |

#### Query Parameters for List Venues

- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 50)
- `sort` (optional): Sort property (default: "name")
- `direction` (optional): Sort direction - "asc" or "desc" (default: "asc")

#### VenueRequest

```json
{
  "name": "string (required, max 255 chars)",
  "address": "string (optional, max 1000 chars)"
}
```

#### VenueResponse

```json
{
  "id": "long",
  "refId": "string (prefix: 'ven-')",
  "name": "string",
  "address": "string",
  "capacity": "integer",
  "createdAt": "string",
  "links": {
    "self": "string",
    "shows": "string"
  }
}
```

### 3. Venue Seats Management

#### Base Path: `/api/v1/venues/{venueId}/seats`

| Method | Endpoint | Description            | Request Body   | Response                       |
| ------ | -------- | ---------------------- | -------------- | ------------------------------ |
| POST   | `/`      | Create seats for venue | SeatMapRequest | BulkSeatCreationResponse (201) |
| GET    | `/`      | Get seat map for venue | -              | SeatMapResponse (200)          |

#### SeatMapRequest

```json
{
  "sections": [
    {
      "sectionId": "string (required)",
      "rows": [
        {
          "rowId": "string (required)",
          "seatCount": "integer (required, min: 1)"
        }
      ]
    }
  ]
}
```

#### BulkSeatCreationResponse

```json
{
  "seatCount": "integer",
  "sectionCount": "integer",
  "venueName": "string",
  "totalCapacity": "integer"
}
```

#### SeatMapResponse

```json
{
  "venueName": "string",
  "totalCapacity": "integer",
  "sections": [
    {
      "sectionId": "string",
      "rows": [
        {
          "rowId": "string",
          "seats": [
            {
              "id": "long",
              "section": "string",
              "row": "string",
              "seatNumber": "string"
            }
          ]
        }
      ]
    }
  ]
}
```

## HTTP Status Codes

| Code | Description                                             |
| ---- | ------------------------------------------------------- |
| 200  | OK - Request successful                                 |
| 201  | Created - Resource created successfully                 |
| 204  | No Content - Request successful, no response body       |
| 400  | Bad Request - Invalid request data or validation errors |
| 404  | Not Found - Resource not found                          |
| 409  | Conflict - Resource conflict (e.g., duplicate data)     |
| 500  | Internal Server Error - Server error                    |

## Validation Rules

### Event Validation

- **title**: Required, maximum 255 characters
- **description**: Optional, maximum 2000 characters
- **category**: Optional, maximum 100 characters

### Show Validation

- **venueId**: Required, must reference existing venue
- **eventId**: Required, must reference existing event
- **startTimestamp**: Required, must be in the future, ISO format
- **durationMinutes**: Required, must be at least 1 minute
- **title**: Automatically inherited from event
- **description**: Automatically inherited from event

### Venue Validation

- **name**: Required, maximum 255 characters
- **address**: Optional, maximum 1000 characters

### Seat Map Validation

- **sections**: Required, non-empty array
- **sectionId**: Required, non-blank string
- **rows**: Required, non-empty array
- **rowId**: Required, non-blank string
- **seatCount**: Required, minimum value of 1

### Pagination Validation

- **page**: Optional integer, defaults to 0
- **size**: Optional integer, defaults to 50
- **sort**: Optional string, entity-specific defaults
- **direction**: Optional, must be "asc" or "desc" (case-insensitive)

## Error Handling

The API uses a global exception handler that provides consistent error responses:

- **Validation Errors**: Returns 400 with field-specific error details
- **Not Found**: Returns 404 with error message
- **Conflict**: Returns 409 with error message
- **Bad Request**: Returns 400 with error message
- **Illegal Argument**: Returns 400 with error message
- **Generic Errors**: Returns 500 with generic error message

## Configuration

- **Default Page Size**: 50 items per page
- **Database**: PostgreSQL
- **Cache**: Redis
- **Migration**: Flyway
- **Timezone**: Asia/Kolkata

## Example Usage

### Create an Event

```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Spring Boot Workshop",
    "description": "Learn Spring Boot fundamentals",
    "category": "Technology"
  }'
```

### List Events with Pagination

```bash
curl "http://localhost:8080/api/v1/events?page=0&size=10&sort=title&direction=asc"
```

### Create a Venue

```bash
curl -X POST http://localhost:8080/api/v1/venues \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Convention Center",
    "address": "123 Main St, City, State"
  }'
```

### Create Seats for a Venue

```bash
curl -X POST http://localhost:8080/api/v1/venues/1/seats \
  -H "Content-Type: application/json" \
  -d '{
    "sections": [
      {
        "sectionId": "A",
        "rows": [
          {
            "rowId": "1",
            "seatCount": 10
          },
          {
            "rowId": "2",
            "seatCount": 10
          }
        ]
      }
    ]
  }'
```
