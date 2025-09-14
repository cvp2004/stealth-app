## Admin API Reference

Base: `/api/v1/admin`

Note: All `/api/v1/admin` routes require header `X-Admin-User: true`.

All list endpoints accept PaginationRequest in the request body unless noted.

### Common

- PaginationRequest:

```json
{
  "page": 0,
  "size": 20,
  "sort": "createdAt",
  "direction": "desc"
}
```

- PaginationResponse: see api-spec.md
- Errors: see api-spec.md

### Venues

- GET `/venue/list`

  - Request: PaginationRequest not required (returns full list if small); current implementation returns list without pagination.
  - Response items:

```json
[
  {
    "id": 1,
    "name": "Grand Hall",
    "address": "123 Main St",
    "capacity": 500
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
          "seats": [
            {
              "id": 11,
              "seat_label": "A1-01"
            }
          ]
        }
      ]
    }
  ]
}
```

- POST `/venue/{id}/seats`
  - Request:

```json
{
  "sections": [
    {
      "sectionId": "A",
      "rows": [
        {
          "rowId": "A1",
          "seats": [{ "section": "A", "row": "A1", "seatNumber": 1 }]
        }
      ]
    }
  ]
}
```

- Response: SeatMap (same as above)

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
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
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

- POST `/event`

  - Request:

```json
{
  "title": "Rock Night",
  "description": "Live bands",
  "category": "MUSIC"
}
```

- Response:

```json
{
  "id": 10,
  "title": "Rock Night",
  "description": "Live bands",
  "category": "MUSIC",
  "status": "CREATED"
}
```

- PATCH `/event/{id}/status/update`
  - Request:

```json
{
  "status": "LIVE"
}
```

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

- Validation: if closing event and LIVE shows exist for event → 400

### Shows

- POST `/show`

  - Request:

```json
{
  "eventId": 10,
  "venueId": 1,
  "startTimestamp": "2025-09-15T19:30:00Z",
  "durationMinutes": 120
}
```

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

  - Request: PaginationRequest
  - Response: PaginationResponse with content mapped to Show summary

```json
{
  "content": [{ "id": 200, "status": "LIVE" }],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

- GET `/show/event/{eventId}/list`

  - Request: PaginationRequest
  - Response: PaginationResponse with content mapped to Show summary

```json
{
  "content": [{ "id": 200, "status": "LIVE" }],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

- PATCH `/show/{id}/status/update`
  - Request: ShowStatusUpdateRequest { status: LIVE|CLOSED|CANCELLED }
  - Response: Show summary mapping

```json
{
  "id": 200,
  "status": "CANCELLED"
}
```

### Users

- GET `/user/{id}`

  - Response:

```json
{
  "id": 101,
  "fullName": "Jane Doe",
  "email": "jane@example.com"
}
```

- GET `/user/email/{email}`

  - Response:

```json
{
  "id": 101,
  "fullName": "Jane Doe",
  "email": "jane@example.com"
}
```

- GET `/user/name/{name}`

  - Response:

```json
{
  "id": 101,
  "fullName": "Jane Doe",
  "email": "jane@example.com"
}
```

- GET `/user/list`
  - Request: PaginationRequest
  - Response: PaginationResponse of { id, fullName, email }

```json
{
  "content": [
    { "id": 101, "fullName": "Jane Doe", "email": "jane@example.com" }
  ],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
}
```

### Bookings

- GET `/booking/{id}`

  - Response:

```json
{
  "id": 9001,
  "user": { "id": 101, "fullName": "Jane Doe", "email": "jane@example.com" },
  "show": {
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
  },
  "status": "CONFIRMED",
  "totalAmount": 1500,
  "createdAt": "2025-09-15T18:45:00Z"
}
```

- GET `/booking/show/{showId}/list`

  - Request: PaginationRequest
  - Response: PaginationResponse of booking mapping

```json
{
  "content": [{ "id": 9001, "status": "CONFIRMED" }],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
}
```

- GET `/booking/event/{eventId}/list`

  - Request: PaginationRequest
  - Response: PaginationResponse of booking mapping

```json
{
  "content": [{ "id": 9001, "status": "CONFIRMED" }],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
}
```

- GET `/booking/venue/{venueId}/list`

  - Request: PaginationRequest
  - Response: PaginationResponse of booking mapping

```json
{
  "content": [{ "id": 9001, "status": "CONFIRMED" }],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
}
```

- GET `/booking/user/{userId}/list`
  - Request: PaginationRequest
  - Response: PaginationResponse of booking mapping

```json
{
  "content": [{ "id": 9001, "status": "CONFIRMED" }],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
}
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

- GET `/ticket/list`

  - Request: PaginationRequest
  - Response: PaginationResponse of ticket mapping

```json
{
  "content": [{ "id": 7001, "price": 1500 }],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
}
```

- GET `/ticket/booking/{bookingId}/list`
  - Request: PaginationRequest
  - Response: PaginationResponse of ticket mapping

```json
{
  "content": [{ "id": 7001, "price": 1500 }],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
}
```

### Payments

- GET `/payment/{id}`

  - Response:

```json
{
  "id": 8001,
  "booking": { "id": 9001 },
  "status": "SUCCESS",
  "amount": 1500,
  "createdAt": "2025-09-15T18:46:00Z"
}
```

- GET `/payment/list`

  - Request: PaginationRequest
  - Response: PaginationResponse of payment mapping

```json
{
  "content": [{ "id": 8001, "status": "SUCCESS", "amount": 1500 }],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
}
```

- GET `/payment/booking/{bookingId}/list`
  - Request: PaginationRequest
  - Response: PaginationResponse of payment mapping

```json
{
  "content": [{ "id": 8001, "status": "SUCCESS", "amount": 1500 }],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
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

  - Request: PaginationRequest
  - Response: PaginationResponse of refund mapping

```json
{
  "content": [{ "id": 8101, "amount": 1500 }],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
}
```

- GET `/refund/booking/{bookingId}/list`
  - Request: PaginationRequest
  - Response: PaginationResponse of refund mapping

```json
{
  "content": [{ "id": 8101, "amount": 1500 }],
  "page": { "number": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
}
```
