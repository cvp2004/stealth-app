# 🎟 Evently API Design Document

> **Event Ticket Management System API Design Guidelines**

This document defines the comprehensive API design standards, conventions, and best practices for the Evently event ticket management platform. Following these guidelines ensures consistency, clarity, and scalability across all API services.

---

## 📋 Table of Contents

- [API Style Guide](#api-style-guide)
- [Resource Design](#resource-design)
- [HTTP Methods & Semantics](#http-methods--semantics)
- [Versioning Strategy](#versioning-strategy)
- [Status Codes & Error Handling](#status-codes--error-handling)
- [Response Structure](#response-structure)
- [Pagination & Filtering](#pagination--filtering)
- [Security & Authentication](#security--authentication)
- [Performance Guidelines](#performance-guidelines)

---

## API Style Guide

### 1. Resource-Oriented Design

Always model APIs around resources (nouns), not actions (verbs). Use plural nouns for collections.

**✅ Good:**

```http
GET /api/v1/venues          # fetch all venues
POST /api/v1/events         # create a new event
GET /api/v1/bookings        # fetch all bookings
```

**❌ Bad:**

```http
GET /api/v1/getVenues
POST /api/v1/createNewEvent
GET /api/v1/fetchBookings
```

### 2. HTTP Methods

Stick to REST semantics for clear intent:

| Method   | Purpose                                   | Example                       |
| -------- | ----------------------------------------- | ----------------------------- |
| `GET`    | Retrieve resources                        | `GET /api/v1/venues/123`      |
| `POST`   | Create new resource                       | `POST /api/v1/events`         |
| `PUT`    | Update entire resource (full replacement) | `PUT /api/v1/venues/123`      |
| `PATCH`  | Update part of a resource                 | `PATCH /api/v1/shows/123`     |
| `DELETE` | Remove a resource                         | `DELETE /api/v1/bookings/456` |

**Example PATCH Request:**

```http
PATCH /api/v1/shows/123
Content-Type: application/json

{
  "startTime": "2025-09-15T18:00:00Z",
  "endTime": "2025-09-15T21:00:00Z"
}
```

### 3. Versioning

Always prefix APIs with a version number to enable backward compatibility.

**Format:** `/api/v{version}/...`

**Example:**

```http
/api/v1/venues     # Current version
/api/v2/venues     # Future breaking changes
```

This allows releasing breaking changes in new versions without disrupting existing clients.

---

## Resource Design

### Core Resources

Based on the database schema, the following resources are available:

| Resource      | Endpoint            | Description                    |
| ------------- | ------------------- | ------------------------------ |
| **Users**     | `/api/v1/users`     | User account management        |
| **Venues**    | `/api/v1/venues`    | Venue information and capacity |
| **Shows**     | `/api/v1/shows`     | Event scheduling and details   |
| **Seats**     | `/api/v1/seats`     | Seat layout and availability   |
| **Bookings**  | `/api/v1/bookings`  | Customer booking transactions  |
| **Payments**  | `/api/v1/payments`  | Payment processing             |
| **Refunds**   | `/api/v1/refunds`   | Refund management              |
| **Attendees** | `/api/v1/attendees` | Event attendance records       |

---

## HTTP Methods & Semantics

### GET Operations

**Retrieve Single Resource:**

```http
GET /api/v1/venues/123
```

**Retrieve Collection:**

```http
GET /api/v1/venues?page=0&size=20&sort=name,asc
```

**Retrieve Related Resources:**

```http
GET /api/v1/venues/123/shows
GET /api/v1/bookings/456/attendees
```

### POST Operations

**Create New Resource:**

```http
POST /api/v1/events
Content-Type: application/json

{
  "title": "Coldplay World Tour",
  "description": "Music concert at Wembley Stadium",
  "venueId": 123,
  "startTime": "2025-09-15T18:00:00Z",
  "endTime": "2025-09-15T21:00:00Z"
}
```

**Complex Operations:**

```http
POST /api/v1/bookings
Content-Type: application/json
Idempotency-Key: 87b7e9d3-f321-4ac2-9db2-122dcf0a5e1f

{
  "userId": 456,
  "showId": 789,
  "seatIds": [101, 102, 103],
  "attendeeDetails": [
    {
      "fullName": "John Doe",
      "email": "john@example.com",
      "age": 25
    }
  ]
}
```

### PUT Operations

**Full Resource Update:**

```http
PUT /api/v1/venues/123
Content-Type: application/json

{
  "name": "Wembley Stadium",
  "address": "Wembley, London HA9 0WS, UK",
  "capacity": 90000
}
```

### PATCH Operations

**Partial Resource Update:**

```http
PATCH /api/v1/shows/123
Content-Type: application/json

{
  "startTime": "2025-09-15T19:00:00Z"
}
```

### DELETE Operations

**Remove Resource:**

```http
DELETE /api/v1/bookings/456
```

---

## Status Codes & Error Handling

### Success Status Codes

| Code             | Meaning                       | Usage                                      |
| ---------------- | ----------------------------- | ------------------------------------------ |
| `200 OK`         | Successful GET or update      | Resource retrieved or updated successfully |
| `201 Created`    | Resource successfully created | New resource created with location header  |
| `204 No Content` | Successful delete             | Resource deleted successfully              |

### Error Status Codes

| Code                        | Meaning                 | Usage                                               |
| --------------------------- | ----------------------- | --------------------------------------------------- |
| `400 Bad Request`           | Invalid input           | Malformed request or validation errors              |
| `401 Unauthorized`          | Authentication required | Missing or invalid authentication                   |
| `403 Forbidden`             | No permission           | Authenticated but lacks permission                  |
| `404 Not Found`             | Resource not found      | Requested resource doesn't exist                    |
| `409 Conflict`              | Conflict                | Business rule violation (e.g., seat already booked) |
| `422 Unprocessable Entity`  | Validation failed       | Request valid but business logic failed             |
| `429 Too Many Requests`     | Rate limit exceeded     | Too many requests in time window                    |
| `500 Internal Server Error` | Server error            | Unexpected backend failure                          |

### Error Response Format

```json
{
  "timestamp": "2025-09-11T01:20:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Venue name is required",
  "path": "/api/v1/venues",
  "details": [
    {
      "field": "name",
      "message": "Venue name cannot be empty"
    }
  ]
}
```

---

## Response Structure

### Response Wrapper Rules

**GET Success Responses:**

- Always wrapped in pagination response structure (with `isPaginated`, `content`, `page`, `sort`, `links`)
- Ensures consistent response schemas across all collection endpoints

**Error Responses (All HTTP Methods):**

- Always wrapped in error response structure (timestamp, status, error, message, path, details)
- Never use pagination wrapper for errors

**POST, PUT, PATCH, DELETE Success Responses:**

- No predefined wrapper, return the resource itself (or 204 No Content where applicable)
- Flexible format determined by implementation

### Success Response (Single Resource)

```json
{
  "id": 1,
  "name": "Wembley Stadium",
  "address": "Wembley, London HA9 0WS, UK",
  "capacity": 90000,
  "createdAt": "2025-01-01T00:00:00Z",
  "links": {
    "self": "/api/v1/venues/1",
    "shows": "/api/v1/venues/1/shows"
  }
}
```

### Success Response (Collection with Pagination)

**Paginated Response (Large Dataset):**

```json
{
  "isPaginated": true,
  "content": [
    {
      "id": 1,
      "name": "Wembley Stadium",
      "capacity": 90000,
      "address": "Wembley, London HA9 0WS, UK"
    },
    {
      "id": 2,
      "name": "O2 Arena",
      "capacity": 20000,
      "address": "Peninsula Square, London SE10 0DX, UK"
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 145,
    "totalPages": 8
  },
  "sort": {
    "property": "name",
    "direction": "asc"
  },
  "links": {
    "self": "/api/v1/venues?page=0&size=20",
    "first": "/api/v1/venues?page=0&size=20",
    "last": "/api/v1/venues?page=7&size=20",
    "next": "/api/v1/venues?page=1&size=20"
  }
}
```

**Non-Paginated Response (Small Dataset):**

```json
{
  "isPaginated": false,
  "content": [
    {
      "id": 1,
      "name": "Sydney Opera House",
      "capacity": 5738,
      "address": "Bennelong Point, Sydney NSW 2000, Australia"
    },
    {
      "id": 2,
      "name": "Royal Albert Hall",
      "capacity": 5272,
      "address": "Kensington Gore, London SW7 2AP, UK"
    }
  ],
  "page": {
    "number": 0,
    "size": 2,
    "totalElements": 2,
    "totalPages": 1
  },
  "sort": {
    "property": "name",
    "direction": "asc"
  },
  "links": {
    "self": "/api/v1/venues"
  }
}
```

---

## Pagination & Filtering

### Pagination Rules

**Rule 1: Single Endpoint for All Responses**

Each collection resource has only one endpoint. The same endpoint is used whether the dataset is small or large. Clients do not need to call a different URL for paginated versus non-paginated data — the behavior is handled automatically by the server.

**Implementation Details:**

- Server automatically determines whether to paginate based on dataset size and configured thresholds
- No separate `/paginated` or `/all` endpoints needed
- Query parameters (`page`, `size`) are optional and handled gracefully
- When `page` and `size` are not provided, server returns all data in a single response

**Benefits:**

- Simplified client integration with consistent endpoint URLs
- Reduced API surface area and documentation complexity
- Automatic optimization based on data size
- Backward compatibility when adding pagination to existing endpoints

**Example:**

```http
# Same endpoint for both scenarios
GET /api/v1/venues          # Returns all venues (small dataset)
GET /api/v1/venues?page=0&size=20  # Returns paginated venues (large dataset)
```

**Rule 2: Always Use Pagination Wrapper**

Every collection response is wrapped inside a pagination response structure. Even when the dataset is small and fits into a single response, the pagination object is still included. In such cases, the metadata shows page = 0, size equal to the actual number of items returned, totalPages = 1, and totalElements equal to the total count. This ensures that the response format remains consistent across all endpoints.

**Implementation Details:**

- All collection responses must include: `isPaginated`, `content`, `page`, `sort`, `links`
- For small datasets: `page.number = 0`, `page.size = actual_count`, `page.totalPages = 1`
- For large datasets: Standard pagination metadata with actual page numbers and counts
- `content` array always contains the actual data items
- `links` object provides navigation URLs (self, first, last, next, prev)

**Benefits:**

- Consistent response schema across all collection endpoints
- Client code can handle all responses with the same parsing logic
- Predictable structure reduces integration complexity
- Future-proof design that scales with data growth

**Rule 3: Explicit "isPaginated" Flag**

Each collection response contains a boolean field `isPaginated`. When the dataset is large and pagination is applied, `isPaginated` is `true`. When the dataset is small and fits entirely in one response, `isPaginated` is `false`. This makes the response self-explanatory, so clients don't need to infer whether pagination is in effect based only on the metadata values.

**Implementation Details:**

- `isPaginated: true` when `totalPages > 1` or when `page.size < totalElements`
- `isPaginated: false` when `totalPages = 1` and all data fits in one response
- Clients can use this flag to determine UI behavior (show pagination controls, etc.)
- Server calculates this value based on actual data size vs. requested page size

**Benefits:**

- Self-documenting responses eliminate guesswork
- Clients can optimize UI based on pagination status
- Clear indication of whether additional pages are available
- Simplified client logic for handling different response types

**Client Usage Example:**

```javascript
// Client can easily determine behavior
if (response.isPaginated) {
  showPaginationControls(response.page);
  enableLoadMoreButton(response.links.next);
} else {
  hidePaginationControls();
  showAllDataMessage(response.page.totalElements);
}
```

### Pagination Parameters

| Parameter | Type    | Default | Description                      |
| --------- | ------- | ------- | -------------------------------- |
| `page`    | integer | 0       | Page number (0-based)            |
| `size`    | integer | 20      | Number of items per page         |
| `sort`    | string  | -       | Sort criteria (e.g., `name,asc`) |

### Filtering Examples

```http
# Basic pagination
GET /api/v1/venues?page=1&size=10

# Sorting
GET /api/v1/venues?sort=name,asc
GET /api/v1/shows?sort=startTime,desc

# Filtering
GET /api/v1/shows?venueId=123&status=ACTIVE
GET /api/v1/bookings?userId=456&status=CONFIRMED
GET /api/v1/events?category=concert&startDate=2025-09-01&endDate=2025-09-30

# Combined
GET /api/v1/events?page=0&size=10&sort=startTime,asc&category=concert&venueId=123
```

---

## Security & Authentication

### Authentication

All APIs must be served over HTTPS with proper authentication:

```http
Authorization: Bearer <jwt-token>
```

### Role-Based Access Control

| Role       | Permissions                        |
| ---------- | ---------------------------------- |
| **Admin**  | Full CRUD access to all resources  |
| **User**   | Create bookings, view own data     |
| **Public** | View public event information only |

### Security Headers

```http
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
```

### Data Protection

- Never expose sensitive details in responses (payment reference IDs, internal IDs)
- Use data masking for PII in logs
- Implement proper input validation and sanitization

---

## Performance Guidelines

### Optimization Strategies

1. **Avoid N+1 Queries**

   - Fetch related data explicitly via dedicated endpoints
   - Use `include` parameter for optional related data

2. **Caching Strategy**

   - Short TTL (5-15 minutes) for frequently requested resources
   - Cache venue lists, event categories, and static data
   - Use ETags for conditional requests

3. **Rate Limiting**
   - Apply rate limiting for high-load endpoints
   - Use debouncing for seat availability checks
   - Implement different limits for different user roles

### Performance Headers

```http
Cache-Control: public, max-age=300
ETag: "abc123def456"
X-Rate-Limit-Limit: 1000
X-Rate-Limit-Remaining: 999
X-Rate-Limit-Reset: 1640995200
```

---

## 🎯 Final Notes

By following these API design practices:

- **APIs remain predictable, consistent, and scalable**
- **Developers can onboard faster with clear conventions**
- **Clients (mobile/web) integrate seamlessly without surprises**
- **System maintains high performance and security standards**
- **Business requirements are met with robust, maintainable code**

This API design ensures the Evently platform can scale effectively while providing an excellent developer experience and reliable service to end users.
