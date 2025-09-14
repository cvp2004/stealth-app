## Evently API Overview

This document gives a concise overview of API domains and shared response conventions. Detailed per-route specs live in `admin-api.md` and `user-api.md`.

### Request Headers

- User APIs (except authentication): require header `X-User-ID: <userId>`
- Admin APIs: require header `X-Admin-User: true`

### Postman Collections

- User collection: [docs/api-docs/postman/Evently User API.postman_collection.json](postman/Evently%20User%20API.postman_collection.json)
- Admin collection: [docs/api-docs/postman/Evently Admin API.postman_collection.json](postman/Evently%20Admin%20API.postman_collection.json)

### Domains and Base

- Base prefix: `/api/v1`
- Admin APIs: management operations
- User APIs: consumer-facing operations (includes auth and booking workflow)

### For detailed API Details Proceed to:

- [Admin API Specification](docs/api-docs/admin-api.md)
- [User API Specification](docs/api-docs/user-api.md)

## JSON Schemas (Rendered)

### PaginationRequest

```json
{
  "page": 0,
  "size": 10,
  "sort": "createdAt",
  "direction": "asc"
}
```

### PaginationResponse (shape)

```json
{
  "content": [],
  "sort": {
    "fields": [{ "direction": "asc", "property": "createdAt" }]
  },
  "links": {
    "next": null,
    "last": "http://localhost:8080/...",
    "self": "http://localhost:8080/...",
    "prev": null,
    "first": "http://localhost:8080/..."
  },
  "page": {
    "totalElements": 0,
    "size": 10,
    "number": 0,
    "totalPages": 0
  },
  "isPaginated": false
}
```

### ErrorResponse

```json
{
  "timestamp": "2025-09-17T10:18:40.788989500Z",
  "status": 409,
  "error": "Conflict",
  "message": "<error message>",
  "path": "<METHOD> /api/v1/...",
  "details": null
}
```

### Auth

Signup Request

```json
{
  "fullName": "Percy Jackson",
  "email": "percy@evently.com",
  "password": "password123"
}
```

Signup Response

```json
{
  "message": "User registered successfully"
}
```

Signin Request

```json
{
  "email": "alice@example.com",
  "password": "password123"
}
```

Signin Response

```json
{
  "userId": 1,
  "fullName": "Alice Johnson",
  "email": "alice@example.com",
  "message": "User signed in successfully"
}
```

Get User by ID Response

```json
{
  "id": 1,
  "fullName": "Alice Johnson",
  "email": "alice@example.com",
  "password": "password123"
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

Create Booking Request

```json
{
  "showId": 1,
  "seats": [{ "section": "A", "row": "R1", "seatNumber": "1" }]
}
```

Booking Response (example)

```json
{
  "createdAt": "2025-09-17T09:51:54.206702Z",
  "id": 1,
  "status": "CONFIRMED",
  "show": {
    "status": "LIVE",
    "venue": {
      "address": "123 Main St, Metropolis",
      "name": "Grand Hall",
      "id": 1
    },
    "id": 3,
    "event": {
      "category": "CONFERENCE",
      "description": "Talks and workshops on cutting-edge tech",
      "id": 2,
      "title": "Tech Conference"
    },
    "durationMinutes": 180,
    "startTimestamp": "2025-09-22T09:51:47.146294Z"
  },
  "totalAmount": 100,
  "user": { "email": "alice@example.com", "fullName": "Alice Johnson", "id": 1 }
}
```

### Ticket

```json
{
  "seat": { "id": 5, "seat_label": "A-R1-3" },
  "id": 5,
  "price": 100,
  "booking": {
    "createdAt": "2025-09-17T09:51:56.206702Z",
    "id": 3,
    "status": "CONFIRMED",
    "show": {
      "status": "LIVE",
      "venue": { "id": 1, "name": "Grand Hall" },
      "id": 3,
      "event": { "id": 2, "title": "Tech Conference" },
      "durationMinutes": 180,
      "startTimestamp": "2025-09-22T09:51:47.146294Z"
    },
    "totalAmount": 300,
    "user": { "id": 1, "fullName": "Alice Johnson" }
  }
}
```

### Payment (User)

Get Payment by ID (User)

```json
{
  "bookingId": 1,
  "id": 1,
  "amount": 100,
  "createdAt": "2025-09-17T09:51:54.206702Z",
  "status": "SUCCESS"
}
```

Process Payment Request (User)

```json
{
  "reservationId": "reservation-123",
  "amount": 100.0
}
```

### Payment (Admin)

Get Payment by ID (Admin)

```json
{
  "id": 1,
  "amount": 100,
  "createdAt": "2025-09-17T07:36:25.230275Z",
  "status": "SUCCESS",
  "booking": {
    "id": 1,
    "status": "CONFIRMED",
    "show": {
      "startTimestamp": "2025-09-22T07:36:18.174709Z",
      "id": 3,
      "event": { "title": "Tech Conference", "id": 2 },
      "venue": { "name": "Grand Hall", "id": 1 }
    },
    "totalAmount": 100,
    "user": {
      "id": 1,
      "email": "alice@example.com",
      "fullName": "Alice Johnson"
    },
    "createdAt": "2025-09-17T07:36:25.230275Z"
  }
}
```

### Refund (User)

Get Refund by ID (User)

```json
{
  "bookingId": 1,
  "id": 1,
  "amount": 100,
  "createdAt": "2025-09-17T10:08:58.677091Z",
  "paymentId": 1
}
```

Cancel Booking Response (User)

```json
{
  "bookingId": 1,
  "message": "Booking cancelled successfully. Refund will be processed within 3-5 business days.",
  "success": true,
  "refundAmount": 100
}
```

### Seat Maps

Admin Venue Seat Map Response

```json
{
  "venueName": "Grand Hall",
  "totalCapacity": 500,
  "sections": [
    {
      "rows": [
        {
          "seats": [
            {
              "seat": "1",
              "row": "R1",
              "id": 1,
              "seat_label": "A-R1-1",
              "section": "A"
            },
            {
              "seat": "2",
              "row": "R1",
              "id": 3,
              "seat_label": "A-R1-2",
              "section": "A"
            }
          ],
          "rowId": "R1"
        }
      ],
      "sectionId": "A"
    }
  ]
}
```

User Show Seats Response

```json
{
  "bookedSeatIds": [7, 8, 9, 10, 11, 12],
  "venueName": "Grand Hall",
  "seatMap": {
    "venueName": "Grand Hall",
    "totalCapacity": 20,
    "sections": [
      {
        "rows": [
          {
            "seats": [
              { "seat_label": "A-R1-1", "id": 1, "status": "AVAILABLE" },
              { "seat_label": "A-R1-4", "id": 7, "status": "BOOKED" }
            ],
            "rowId": "R1"
          }
        ],
        "sectionId": "A"
      }
    ]
  },
  "eventName": "Rock Night",
  "showName": "Show 1",
  "showId": 1
}
```

## Endpoint Inventory (Tables)

### Admin Endpoints

| Method | Path                            | Description           |
| ------ | ------------------------------- | --------------------- |
| GET    | /api/v1/admin/venue/list        | List venues           |
| GET    | /api/v1/admin/venue/{id}        | Get venue by id       |
| GET    | /api/v1/admin/venue/name/{name} | Get venue by name     |
| POST   | /api/v1/admin/venue             | Create venue          |
| GET    | /api/v1/admin/venue/{id}/seats  | Get venue seat map    |
| POST   | /api/v1/admin/venue/{id}/seats  | Create venue seat map |

| Method | Path                                   | Description             |
| ------ | -------------------------------------- | ----------------------- |
| GET    | /api/v1/admin/event/list               | List events (paginated) |
| GET    | /api/v1/admin/event/{id}               | Get event by id         |
| GET    | /api/v1/admin/event/title/{title}      | Get event by title      |
| POST   | /api/v1/admin/event                    | Create event            |
| PATCH  | /api/v1/admin/event/{id}/status/update | Update event status     |

| Method | Path                                    | Description         |
| ------ | --------------------------------------- | ------------------- |
| POST   | /api/v1/admin/show                      | Create show         |
| GET    | /api/v1/admin/show/{id}                 | Get show by id      |
| GET    | /api/v1/admin/show/venue/{venueId}/list | List shows by venue |
| GET    | /api/v1/admin/show/event/{eventId}/list | List shows by event |
| PATCH  | /api/v1/admin/show/{id}/status/update   | Update show status  |

| Method | Path                             | Description            |
| ------ | -------------------------------- | ---------------------- |
| GET    | /api/v1/admin/user/{id}          | Get user by id         |
| GET    | /api/v1/admin/user/email/{email} | Get user by email      |
| GET    | /api/v1/admin/user/name/{name}   | Get user by name       |
| GET    | /api/v1/admin/user/list          | List users (paginated) |

| Method | Path                                       | Description            |
| ------ | ------------------------------------------ | ---------------------- |
| GET    | /api/v1/admin/booking/{id}                 | Get booking by id      |
| GET    | /api/v1/admin/booking/show/{showId}/list   | List bookings by show  |
| GET    | /api/v1/admin/booking/event/{eventId}/list | List bookings by event |
| GET    | /api/v1/admin/booking/venue/{venueId}/list | List bookings by venue |
| GET    | /api/v1/admin/booking/user/{userId}/list   | List bookings by user  |

| Method | Path                                          | Description              |
| ------ | --------------------------------------------- | ------------------------ |
| GET    | /api/v1/admin/ticket/{id}                     | Get ticket by id         |
| GET    | /api/v1/admin/ticket/list                     | List tickets (paginated) |
| GET    | /api/v1/admin/ticket/booking/{bookingId}/list | List tickets by booking  |

| Method | Path                                           | Description               |
| ------ | ---------------------------------------------- | ------------------------- |
| GET    | /api/v1/admin/payment/{id}                     | Get payment by id         |
| GET    | /api/v1/admin/payment/list                     | List payments (paginated) |
| GET    | /api/v1/admin/payment/booking/{bookingId}/list | List payments by booking  |

| Method | Path                                          | Description              |
| ------ | --------------------------------------------- | ------------------------ |
| GET    | /api/v1/admin/refund/{id}                     | Get refund by id         |
| GET    | /api/v1/admin/refund/list                     | List refunds (paginated) |
| GET    | /api/v1/admin/refund/booking/{bookingId}/list | List refunds by booking  |

### User Endpoints (incl. Auth)

| Method | Path                       | Description      |
| ------ | -------------------------- | ---------------- |
| POST   | /api/v1/auth/signup        | Sign up          |
| POST   | /api/v1/auth/signin        | Sign in          |
| GET    | /api/v1/auth/user/{userId} | Get user profile |

| Method | Path                           | Description        |
| ------ | ------------------------------ | ------------------ |
| GET    | /api/v1/user/venue/list        | List venues        |
| GET    | /api/v1/user/venue/{id}        | Get venue by id    |
| GET    | /api/v1/user/venue/name/{name} | Get venue by name  |
| GET    | /api/v1/user/venue/{id}/seats  | Get venue seat map |

| Method | Path                             | Description             |
| ------ | -------------------------------- | ----------------------- |
| GET    | /api/v1/user/event/list          | List events (paginated) |
| GET    | /api/v1/user/event/{id}          | Get event by id         |
| GET    | /api/v1/user/event/title/{title} | Get event by title      |

| Method | Path                                                   | Description                     |
| ------ | ------------------------------------------------------ | ------------------------------- |
| GET    | /api/v1/user/show/{id}                                 | Get show by id                  |
| GET    | /api/v1/user/show/venue/{venueId}/list                 | List shows by venue             |
| GET    | /api/v1/user/show/event/{eventId}/list                 | List shows by event             |
| GET    | /api/v1/user/show/venue/{venueId}/event/{eventId}/list | List shows by venue+event       |
| GET    | /api/v1/user/show/{showId}/seats                       | Get show seat map with statuses |

| Method | Path                         | Description                    |
| ------ | ---------------------------- | ------------------------------ |
| POST   | /api/v1/user/booking         | Create booking (reservation)   |
| POST   | /api/v1/user/booking/payment | Process booking payment        |
| GET    | /api/v1/user/booking/{id}    | Get booking by id              |
| GET    | /api/v1/user/booking/list    | List user bookings (paginated) |
| DELETE | /api/v1/user/booking/cancel  | Cancel booking                 |

| Method | Path                                     | Description             |
| ------ | ---------------------------------------- | ----------------------- |
| GET    | /api/v1/user/tickets/{id}                | Get ticket by id        |
| GET    | /api/v1/user/tickets/booking/{bookingId} | List tickets by booking |

| Method | Path                                    | Description               |
| ------ | --------------------------------------- | ------------------------- |
| GET    | /api/v1/user/payment/{id}               | Get payment by id         |
| GET    | /api/v1/user/payment/list               | List payments (paginated) |
| GET    | /api/v1/user/payment/show/{showId}/list | List payments by show     |

| Method | Path                                   | Description              |
| ------ | -------------------------------------- | ------------------------ |
| GET    | /api/v1/user/refund/{id}               | Get refund by id         |
| GET    | /api/v1/user/refund/list               | List refunds (paginated) |
| GET    | /api/v1/user/refund/show/{showId}/list | List refunds by show     |
