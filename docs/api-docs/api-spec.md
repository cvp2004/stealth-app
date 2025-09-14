## Evently API Overview

This document gives a concise overview of API domains and shared response conventions. Detailed per-route specs live in `admin-api.md` and `user-api.md`.

### Domains and Base

- Base prefix: `/api/v1`
- Admin APIs: management operations
- User APIs: consumer-facing operations (includes auth and booking workflow)

### Common Request/Response Conventions

- Controllers shape responses as JSON maps exposing only necessary fields; models are not returned directly.

- PaginationRequest (request body for list endpoints):

  - page: integer (>= 0)
  - size: integer (> 0)
  - sort: string (must be `createdAt`)
  - direction: string (`asc` | `desc`)

- PaginationResponse (all list endpoints):

  - isPaginated: boolean
  - content: array<object>
  - page: { number, size, totalElements, totalPages }
  - sort: { fields: [ { property, direction } ] }
  - links: { self, first, last, next, prev }

- ErrorResponse (handled globally):
  - message: string
  - code: string
  - timestamp: string (ISO-8601)
  - path: string
  - fieldErrors?: array<{ field, message }>

### Resource Summary Shapes

- Venue: { id, name, address, capacity }
- Event: { id, title, description, category, status }
- Show: { id, venueId|venue, eventId|event, startTimestamp, durationMinutes, status }
- Booking: { id, user|userId, show|showId, status, totalAmount, createdAt }
- Ticket: { id, booking, seat, price }
- Payment: { id, bookingId|booking, status, amount, createdAt }
- Refund: { id, bookingId, paymentId, amount, createdAt }
- SeatMap (with seat status): { venueName, totalCapacity, sections: [ { sectionId, rows: [ { rowId, seats: [ { id, seat_label, status } ] } ] } ] }

Proceed to:

- Admin: `admin-api.md`
- User (incl. Auth): `user-api.md`

## JSON Schemas (Rendered)

### PaginationRequest

```json
{
  "page": 0,
  "size": 20,
  "sort": "createdAt",
  "direction": "desc"
}
```

### PaginationResponse

```json
{
  "isPaginated": true,
  "content": [{ "...resourceFields": "..." }],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 123,
    "totalPages": 7
  },
  "sort": {
    "fields": [{ "property": "createdAt", "direction": "desc" }]
  },
  "links": {
    "self": "string",
    "first": "string",
    "last": "string",
    "next": "string",
    "prev": "string"
  }
}
```

### ErrorResponse

```json
{
  "message": "string",
  "code": "string",
  "timestamp": "2025-01-15T10:30:00Z",
  "path": "/api/v1/...",
  "fieldErrors": [{ "field": "string", "message": "string" }]
}
```

### Venue

```json
{ "id": 1, "name": "string", "address": "string", "capacity": 0 }
```

### Event

```json
{
  "id": 1,
  "title": "string",
  "description": "string",
  "category": "string",
  "status": "CREATED|LIVE|CLOSED"
}
```

### Show (summary)

```json
{
  "id": 1,
  "venueId": 1,
  "eventId": 1,
  "startTimestamp": "2025-07-15T20:00:00Z",
  "durationMinutes": 120,
  "status": "LIVE|CLOSED|CANCELLED"
}
```

### Show (embedded venue/event)

```json
{
  "id": 1,
  "venue": { "id": 1, "name": "string", "address": "string" },
  "event": {
    "id": 1,
    "title": "string",
    "description": "string",
    "category": "string"
  },
  "startTimestamp": "2025-07-15T20:00:00Z",
  "durationMinutes": 120,
  "status": "LIVE|CLOSED|CANCELLED"
}
```

### Booking

```json
{
  "id": 1,
  "user": { "id": 1, "fullName": "string", "email": "string" },
  "show": {
    "id": 1,
    "venue": { "id": 1, "name": "string", "address": "string" },
    "event": {
      "id": 1,
      "title": "string",
      "description": "string",
      "category": "string"
    },
    "startTimestamp": "2025-07-15T20:00:00Z",
    "durationMinutes": 120,
    "status": "LIVE|CLOSED|CANCELLED"
  },
  "status": "CONFIRMED|CANCELLED",
  "totalAmount": 450.0,
  "createdAt": "2025-07-01T10:00:00Z"
}
```

### Ticket

```json
{
  "id": 1,
  "booking": { "id": 1 },
  "seat": { "id": 1, "seat_label": "A-1-10" },
  "price": 120.0
}
```

### Payment

```json
{
  "id": 1,
  "booking": { "id": 1 },
  "status": "SUCCESS|FAILED",
  "amount": 450.0,
  "createdAt": "2025-07-01T10:05:00Z"
}
```

### Refund

```json
{
  "id": 1,
  "bookingId": 1,
  "paymentId": 10,
  "amount": 450.0,
  "createdAt": "2025-07-02T09:00:00Z"
}
```

### SeatMap (with seat status)

```json
{
  "venueName": "string",
  "totalCapacity": 1000,
  "sections": [
    {
      "sectionId": "S1",
      "rows": [
        {
          "rowId": "R1",
          "seats": [
            { "id": 1, "seat_label": "S1-R1-1", "status": "BOOKED|AVAILABLE" }
          ]
        }
      ]
    }
  ]
}
```

## Endpoint Inventory (Tables)

### Admin Endpoints

| Method | Path                                           | Description               |
| ------ | ---------------------------------------------- | ------------------------- |
| GET    | /api/v1/admin/venue/list                       | List venues               |
| GET    | /api/v1/admin/venue/{id}                       | Get venue by id           |
| GET    | /api/v1/admin/venue/name/{name}                | Get venue by name         |
| GET    | /api/v1/admin/venue/{id}/seats                 | Get venue seat map        |
| POST   | /api/v1/admin/venue/{id}/seats                 | Create venue seat map     |

| Method | Path                                           | Description               |
| ------ | ---------------------------------------------- | ------------------------- |
| GET    | /api/v1/admin/event/list                       | List events (paginated)   |
| GET    | /api/v1/admin/event/{id}                       | Get event by id           |
| GET    | /api/v1/admin/event/title/{title}              | Get event by title        |
| POST   | /api/v1/admin/event                            | Create event              |
| PATCH  | /api/v1/admin/event/{id}/status/update         | Update event status       |

| Method | Path                                           | Description               |
| ------ | ---------------------------------------------- | ------------------------- |
| POST   | /api/v1/admin/show                             | Create show               |
| GET    | /api/v1/admin/show/{id}                        | Get show by id            |
| GET    | /api/v1/admin/show/venue/{venueId}/list        | List shows by venue       |
| GET    | /api/v1/admin/show/event/{eventId}/list        | List shows by event       |
| PATCH  | /api/v1/admin/show/{id}/status/update          | Update show status        |

| Method | Path                                           | Description               |
| ------ | ---------------------------------------------- | ------------------------- |
| GET    | /api/v1/admin/user/{id}                        | Get user by id            |
| GET    | /api/v1/admin/user/email/{email}               | Get user by email         |
| GET    | /api/v1/admin/user/name/{name}                 | Get user by name          |
| GET    | /api/v1/admin/user/list                        | List users (paginated)    |

| Method | Path                                           | Description               |
| ------ | ---------------------------------------------- | ------------------------- |
| GET    | /api/v1/admin/booking/{id}                     | Get booking by id         |
| GET    | /api/v1/admin/booking/show/{showId}/list       | List bookings by show     |
| GET    | /api/v1/admin/booking/event/{eventId}/list     | List bookings by event    |
| GET    | /api/v1/admin/booking/venue/{venueId}/list     | List bookings by venue    |
| GET    | /api/v1/admin/booking/user/{userId}/list       | List bookings by user     |

| Method | Path                                           | Description               |
| ------ | ---------------------------------------------- | ------------------------- |
| GET    | /api/v1/admin/ticket/{id}                      | Get ticket by id          |
| GET    | /api/v1/admin/ticket/list                      | List tickets (paginated)  |
| GET    | /api/v1/admin/ticket/booking/{bookingId}/list  | List tickets by booking   |

| Method | Path                                           | Description               |
| ------ | ---------------------------------------------- | ------------------------- |
| GET    | /api/v1/admin/payment/{id}                     | Get payment by id         |
| GET    | /api/v1/admin/payment/list                     | List payments (paginated) |
| GET    | /api/v1/admin/payment/booking/{bookingId}/list | List payments by booking  |

| Method | Path                                           | Description               |
| ------ | ---------------------------------------------- | ------------------------- |
| GET    | /api/v1/admin/refund/{id}                      | Get refund by id          |
| GET    | /api/v1/admin/refund/list                      | List refunds (paginated)  |
| GET    | /api/v1/admin/refund/booking/{bookingId}/list  | List refunds by booking   |

### User Endpoints (incl. Auth)

| Method | Path                                                   | Description                     |
| ------ | ------------------------------------------------------ | ------------------------------- |
| POST   | /api/v1/auth/signup                                    | Sign up                         |
| POST   | /api/v1/auth/signin                                    | Sign in                         |
| GET    | /api/v1/auth/user/{userId}                             | Get user profile                |

| Method | Path                                                   | Description                     |
| ------ | ------------------------------------------------------ | ------------------------------- |
| GET    | /api/v1/user/venue/list                                | List venues                     |
| GET    | /api/v1/user/venue/{id}                                | Get venue by id                 |
| GET    | /api/v1/user/venue/name/{name}                         | Get venue by name               |
| GET    | /api/v1/user/venue/{id}/seats                          | Get venue seat map              |

| Method | Path                                                   | Description                     |
| ------ | ------------------------------------------------------ | ------------------------------- |
| GET    | /api/v1/user/event/list                                | List events (paginated)         |
| GET    | /api/v1/user/event/{id}                                | Get event by id                 |
| GET    | /api/v1/user/event/title/{title}                       | Get event by title              |

| Method | Path                                                   | Description                     |
| ------ | ------------------------------------------------------ | ------------------------------- |
| GET    | /api/v1/user/show/{id}                                 | Get show by id                  |
| GET    | /api/v1/user/show/venue/{venueId}/list                 | List shows by venue             |
| GET    | /api/v1/user/show/event/{eventId}/list                 | List shows by event             |
| GET    | /api/v1/user/show/venue/{venueId}/event/{eventId}/list | List shows by venue+event       |
| GET    | /api/v1/user/show/{showId}/seats                       | Get show seat map with statuses |

| Method | Path                                                   | Description                     |
| ------ | ------------------------------------------------------ | ------------------------------- |
| POST   | /api/v1/user/booking                                   | Create booking (reservation)    |
| POST   | /api/v1/user/booking/payment                           | Process booking payment         |
| GET    | /api/v1/user/booking/{id}                              | Get booking by id               |
| GET    | /api/v1/user/booking/list                              | List user bookings (paginated)  |
| DELETE | /api/v1/user/booking/cancel                            | Cancel booking                  |

| Method | Path                                                   | Description                     |
| ------ | ------------------------------------------------------ | ------------------------------- |
| GET    | /api/v1/user/tickets/{id}                              | Get ticket by id                |
| GET    | /api/v1/user/tickets/booking/{bookingId}               | List tickets by booking         |

| Method | Path                                                   | Description                     |
| ------ | ------------------------------------------------------ | ------------------------------- |
| GET    | /api/v1/user/payment/{id}                              | Get payment by id               |
| GET    | /api/v1/user/payment/list                              | List payments (paginated)       |
| GET    | /api/v1/user/payment/show/{showId}/list                | List payments by show           |

| Method | Path                                                   | Description                     |
| ------ | ------------------------------------------------------ | ------------------------------- |
| GET    | /api/v1/user/refund/{id}                               | Get refund by id                |
| GET    | /api/v1/user/refund/list                               | List refunds (paginated)        |
| GET    | /api/v1/user/refund/show/{showId}/list                 | List refunds by show            |
