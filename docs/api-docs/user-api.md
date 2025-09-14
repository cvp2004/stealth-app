## User API Reference

Base: `/api/v1/user` (Auth base: `/api/v1/auth`)

Note: All `/api/v1/user` routes require header `X-User-ID: <user_id>`.

All list endpoints accept PaginationRequest in body. See api-spec.md for shared schemas.

### Auth

- POST `/api/v1/auth/signup`

  - Request:

```json
{
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "password": "strongPassword123"
}
```

- Response:

```json
{
  "id": 101,
  "fullName": "Jane Doe",
  "email": "jane@example.com"
}
```

- POST `/api/v1/auth/signin`

  - Request:

```json
{
  "email": "jane@example.com",
  "password": "strongPassword123"
}
```

- Response:

```json
{
  "id": 101,
  "fullName": "Jane Doe",
  "email": "jane@example.com"
}
```

Caller must send `X-User-ID` header in subsequent requests

- GET `/api/v1/auth/user/{userId}`
  - Response:

```json
{
  "id": 101,
  "fullName": "Jane Doe",
  "email": "jane@example.com"
}
```

### Venues

- GET `/venue/list`

  - Response:

```json
[
  {
    "id": 1,
    "name": "Grand Hall",
    "address": "123 Main St",
    "capacity": 500
  },
  {
    "id": 2,
    "name": "Open Air",
    "address": "Park Ave",
    "capacity": 1200
  }
]
```

- GET `/venue/{id}`

  - Response:

```json
{
  "id": 1,
  "name": "Grand Hall",
  "address": "123 Main St",
  "capacity": 500
}
```

- GET `/venue/name/{name}`

  - Response:

```json
{
  "id": 1,
  "name": "Grand Hall",
  "address": "123 Main St",
  "capacity": 500
}
```

- GET `/venue/{id}/seats`
  - Response:

```json
{
  "venueName": "Grand Hall",
  "totalCapacity": 500,
  "sections": [
    {
      "sectionId": "A",
      "rows": [
        {
          "rowId": "A1",
          "seats": [{ "id": 11, "seat_label": "A1-01" }]
        }
      ]
    }
  ]
}
```

### Events

- GET `/event/list`

  - Request:

```json
{
  "category": "MUSIC",
  "page": 0,
  "size": 20,
  "sort": "createdAt",
  "direction": "desc"
}
```

- Response:

```json
{
  "content": [
    {
      "id": 10,
      "title": "Rock Night",
      "description": "Live bands",
      "category": "MUSIC",
      "status": "LIVE"
    }
  ],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 },
  ...
}
```

- GET `/event/{id}`

  - Response:

```json
{
  "id": 10,
  "title": "Rock Night",
  "description": "Live bands",
  "category": "MUSIC",
  "status": "LIVE"
}
```

- GET `/event/title/{title}`
  - Response:

```json
{
  "id": 10,
  "title": "Rock Night",
  "description": "Live bands",
  "category": "MUSIC",
  "status": "LIVE"
}
```

### Shows

- GET `/show/{id}`

  - Response:

```json
{
  "id": 200,
  "venue": { "id": 1, "name": "Grand Hall", "address": "123 Main St" },
  "event": {
    "id": 10,
    "title": "Rock Night",
    "description": "Live bands",
    "category": "MUSIC"
  },
  "startTimestamp": "2025-09-15T19:30:00Z",
  "durationMinutes": 120,
  "status": "LIVE"
}
```

- GET `/show/venue/{venueId}/list`

  - Request:

```json
{ "page": 0, "size": 20, "sort": "createdAt", "direction": "desc" }
```

- Response:

```json
{
  "content": [
    {
      "id": 200,
      "venue": { "id": 1, "name": "Grand Hall", "address": "123 Main St" },
      "event": {
        "id": 10,
        "title": "Rock Night",
        "description": "Live bands",
        "category": "MUSIC"
      },
      "startTimestamp": "2025-09-15T19:30:00Z",
      "durationMinutes": 120,
      "status": "LIVE"
    }
  ],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 },
  ...
}
```

- GET `/show/event/{eventId}/list`

  - Request:

```json
{ "page": 0, "size": 20, "sort": "createdAt", "direction": "desc" }
```

- Response:

```json
{
  "content": [
    {
      "id": 200,
      "venue": { "id": 1, "name": "Grand Hall", "address": "123 Main St" },
      "event": {
        "id": 10,
        "title": "Rock Night",
        "description": "Live bands",
        "category": "MUSIC"
      },
      "startTimestamp": "2025-09-15T19:30:00Z",
      "durationMinutes": 120,
      "status": "LIVE"
    }
  ],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 },
  ...
}
```

- GET `/show/venue/{venueId}/event/{eventId}/list`
  - Request:

```json
{ "page": 0, "size": 20, "sort": "createdAt", "direction": "desc" }
```

- Response:

```json
{
  "content": [
    {
      "id": 200,
      "venue": { "id": 1, "name": "Grand Hall", "address": "123 Main St" },
      "event": {
        "id": 10,
        "title": "Rock Night",
        "description": "Live bands",
        "category": "MUSIC"
      },
      "startTimestamp": "2025-09-15T19:30:00Z",
      "durationMinutes": 120,
      "status": "LIVE"
    }
  ],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 },
  ...
}
```

### Booking Workflow

- GET `/show/{showId}/seats`

  - Response:

```json
{
  "showId": 200,
  "showName": "Rock Night - Evening Show",
  "eventName": "Rock Night",
  "venueName": "Grand Hall",
  "bookedSeatIds": [11, 12],
  "seatMap": {
    "venueName": "Grand Hall",
    "totalCapacity": 500,
    "sections": [
      {
        "sectionId": "A",
        "rows": [
          {
            "rowId": "A1",
            "seats": [
              { "id": 11, "seat_label": "A1-01", "status": "BOOKED" },
              { "id": 12, "seat_label": "A1-02", "status": "AVAILABLE" }
            ]
          }
        ]
      }
    ]
  }
}
```

- POST `/booking`

  - Request:

```json
{
  "showId": 200,
  "seats": [{ "section": "A", "row": "A1", "seatNumber": 1 }]
}
```

- Response:

```json
{
  "reservationId": "resv_abc123",
  "amount": 1500,
  "ttlSeconds": 300
}
```

- POST `/booking/payment`
  - Request:

```json
{ "reservationId": "resv_abc123", "amount": 1500 }
```

- Response:

```json
{ "bookingId": 9001, "status": "CONFIRMED" }
```

### Bookings

- GET `/booking/{id}`

  - Response:

```json
{
  "id": 9001,
  "userId": 101,
  "showId": 200,
  "status": "CONFIRMED",
  "totalAmount": 1500,
  "createdAt": "2025-09-15T18:45:00Z"
}
```

- GET `/booking/list`

  - Request:

```json
{ "page": 0, "size": 20, "sort": "createdAt", "direction": "desc" }
```

- Response:

```json
{
  "content": [
    {
      "id": 9001,
      "userId": 101,
      "showId": 200,
      "status": "CONFIRMED",
      "totalAmount": 1500,
      "createdAt": "2025-09-15T18:45:00Z"
    }
  ],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 },
  ...
}
```

- DELETE `/booking/cancel`
- Request:

```json
{ "bookingId": 9001 }
```

- Response:

```json
{ "bookingId": 9001, "status": "CANCELLED" }
```

### Tickets

- GET `/ticket/{id}`

  - Response:

```json
{
  "id": 7001,
  "booking": { "id": 9001 },
  "seat": { "id": 11, "seat_label": "A1-01" },
  "price": 1500
}
```

- GET `/tickets/booking/{bookingId}`
  - Request:

```json
{ "page": 0, "size": 20, "sort": "createdAt", "direction": "desc" }
```

- Response:

```json
{
  "content": [
    {
      "id": 7001,
      "booking": { "id": 9001 },
      "seat": { "id": 11, "seat_label": "A1-01" },
      "price": 1500
    }
  ],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 },
  ...
}
```

### Payments

- GET `/payment/{id}`

  - Response:

```json
{
  "id": 8001,
  "bookingId": 9001,
  "status": "SUCCESS",
  "amount": 1500,
  "createdAt": "2025-09-15T18:46:00Z"
}
```

- GET `/payment/list`

  - Request:

```json
{ "page": 0, "size": 20, "sort": "createdAt", "direction": "desc" }
```

- Response:

```json
{
  "content": [
    {
      "id": 8001,
      "bookingId": 9001,
      "status": "SUCCESS",
      "amount": 1500,
      "createdAt": "2025-09-15T18:46:00Z"
    }
  ],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 },
  ...
}
```

- GET `/payment/show/{showId}/list`
  - Request:

```json
{ "page": 0, "size": 20, "sort": "createdAt", "direction": "desc" }
```

- Response:

```json
{
  "content": [
    {
      "id": 8001,
      "bookingId": 9001,
      "status": "SUCCESS",
      "amount": 1500,
      "createdAt": "2025-09-15T18:46:00Z"
    }
  ],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 },
  ...
}
```

### Refunds

- GET `/refund/{id}`

  - Response:

```json
{
  "id": 8101,
  "bookingId": 9001,
  "paymentId": 8001,
  "amount": 1500,
  "createdAt": "2025-09-15T19:00:00Z"
}
```

- GET `/refund/list`

  - Request:

```json
{ "page": 0, "size": 20, "sort": "createdAt", "direction": "desc" }
```

- Response:

```json
{
  "content": [
    {
      "id": 8101,
      "bookingId": 9001,
      "paymentId": 8001,
      "amount": 1500,
      "createdAt": "2025-09-15T19:00:00Z"
    }
  ],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 },
  ...
}
```

- GET `/refund/show/{showId}/list`
  - Request:

```json
{ "page": 0, "size": 20, "sort": "createdAt", "direction": "desc" }
```

- Response:

```json
{
  "content": [
    {
      "id": 8101,
      "bookingId": 9001,
      "paymentId": 8001,
      "amount": 1500,
      "createdAt": "2025-09-15T19:00:00Z"
    }
  ],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 },
  ...
}
```
