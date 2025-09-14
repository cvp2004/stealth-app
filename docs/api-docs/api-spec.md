# Evently API Specification

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [User APIs](#user-apis)
   - [Authentication](#user-authentication)
   - [Events](#user-events)
   - [Shows](#user-shows)
   - [Venues](#user-venues)
   - [Bookings](#user-bookings)
   - [Tickets](#user-tickets)
   - [Payments](#user-payments)
   - [Refunds](#user-refunds)
4. [Admin APIs](#admin-apis)
   - [Events](#admin-events)
   - [Shows](#admin-shows)
   - [Venues](#admin-venues)
   - [Users](#admin-users)
   - [Bookings](#admin-bookings)
   - [Tickets](#admin-tickets)
   - [Payments](#admin-payments)
   - [Refunds](#admin-refunds)
5. [Common Response Formats](#common-response-formats)
6. [Error Handling](#error-handling)

## Overview

Evently is a comprehensive event management platform that allows users to discover, book, and manage events, shows, and tickets. The API provides endpoints for both regular users and administrators to interact with the system.

**Base URL:** `http://localhost:8080/api`

## Authentication

### User APIs

All user API requests (except authentication endpoints) require the `X-User-ID` header containing a valid user ID.

### Admin APIs

All admin API requests require the `X-Admin-User` header set to `true` for authorization.

### Public APIs

Authentication endpoints (`/api/v1/auth/**`) are publicly accessible and do not require any headers.

## User APIs

### User Authentication

| Method | Endpoint            | Description                            |
| ------ | ------------------- | -------------------------------------- |
| POST   | `/v1/auth/register` | Register a new user account            |
| POST   | `/v1/auth/login`    | Authenticate user and get access token |

### User Events

| Method | Endpoint                        | Description                                   |
| ------ | ------------------------------- | --------------------------------------------- |
| GET    | `/v1/user/events`               | Get paginated list of events                  |
| GET    | `/v1/user/events/{id}`          | Get event details by ID                       |
| GET    | `/v1/user/events/title/{title}` | Get event details by title (LIVE/CLOSED only) |

### User Shows

| Method | Endpoint                                         | Description                        |
| ------ | ------------------------------------------------ | ---------------------------------- |
| GET    | `/v1/user/shows`                                 | Get paginated list of shows        |
| GET    | `/v1/user/shows/{id}`                            | Get show details by ID             |
| GET    | `/v1/user/shows/{id}/seats`                      | Get available seats for a show     |
| GET    | `/v1/user/shows/venue/{venueId}`                 | Get shows by venue ID              |
| GET    | `/v1/user/shows/event/{eventId}`                 | Get shows by event ID              |
| GET    | `/v1/user/shows/venue/{venueId}/event/{eventId}` | Get shows by venue ID and event ID |

### User Venues

| Method | Endpoint               | Description                  |
| ------ | ---------------------- | ---------------------------- |
| GET    | `/v1/user/venues`      | Get paginated list of venues |
| GET    | `/v1/user/venues/{id}` | Get venue details by ID      |

### User Bookings

| Method | Endpoint                                            | Description                               |
| ------ | --------------------------------------------------- | ----------------------------------------- |
| GET    | `/v1/user/bookings`                                 | Get user's bookings with pagination       |
| GET    | `/v1/user/bookings/{id}`                            | Get specific booking details              |
| GET    | `/v1/user/bookings/venue/{venueId}`                 | Get user's bookings by venue ID           |
| GET    | `/v1/user/bookings/show/{showId}`                   | Get user's bookings by show ID            |
| GET    | `/v1/user/bookings/event/{eventId}`                 | Get user's bookings by event ID           |
| GET    | `/v1/user/bookings/venue/{venueId}/show/{showId}`   | Get user's bookings by venue and show ID  |
| GET    | `/v1/user/bookings/venue/{venueId}/event/{eventId}` | Get user's bookings by venue and event ID |
| GET    | `/v1/user/bookings/show/{showId}/event/{eventId}`   | Get user's bookings by show and event ID  |
| POST   | `/v1/user/bookings`                                 | Create a new booking (reserve seats)      |
| POST   | `/v1/user/bookings/payment`                         | Process payment for a booking             |
| DELETE | `/v1/user/bookings/cancel`                          | Cancel an existing booking                |

### User Tickets

| Method | Endpoint                | Description                        |
| ------ | ----------------------- | ---------------------------------- |
| GET    | `/v1/user/tickets`      | Get user's tickets with pagination |
| GET    | `/v1/user/tickets/{id}` | Get specific ticket details        |

### User Payments

| Method | Endpoint                 | Description                  |
| ------ | ------------------------ | ---------------------------- |
| GET    | `/v1/user/payments`      | Get user's payment history   |
| GET    | `/v1/user/payments/{id}` | Get specific payment details |

### User Refunds

| Method | Endpoint                | Description                        |
| ------ | ----------------------- | ---------------------------------- |
| GET    | `/v1/user/refunds`      | Get user's refunds with pagination |
| GET    | `/v1/user/refunds/{id}` | Get specific refund details        |

## Admin APIs

### Admin Events

| Method | Endpoint                         | Description                               |
| ------ | -------------------------------- | ----------------------------------------- |
| GET    | `/v1/admin/events`               | Get all events with pagination            |
| GET    | `/v1/admin/events/{id}`          | Get event details by ID                   |
| GET    | `/v1/admin/events/title/{title}` | Get event details by title (all statuses) |
| POST   | `/v1/admin/events`               | Create a new event                        |
| PUT    | `/v1/admin/events/{id}`          | Update an existing event                  |
| DELETE | `/v1/admin/events/{id}`          | Delete an event                           |

### Admin Shows

| Method | Endpoint                                          | Description                        |
| ------ | ------------------------------------------------- | ---------------------------------- |
| GET    | `/v1/admin/shows`                                 | Get all shows with pagination      |
| GET    | `/v1/admin/shows/{id}`                            | Get show details                   |
| GET    | `/v1/admin/shows/venue/{venueId}`                 | Get shows by venue ID              |
| GET    | `/v1/admin/shows/event/{eventId}`                 | Get shows by event ID              |
| GET    | `/v1/admin/shows/venue/{venueId}/event/{eventId}` | Get shows by venue ID and event ID |
| POST   | `/v1/admin/shows`                                 | Create a new show                  |
| PUT    | `/v1/admin/shows/{id}`                            | Update an existing show            |
| DELETE | `/v1/admin/shows/{id}`                            | Delete a show                      |

### Admin Venues

| Method | Endpoint                | Description                    |
| ------ | ----------------------- | ------------------------------ |
| GET    | `/v1/admin/venues`      | Get all venues with pagination |
| GET    | `/v1/admin/venues/{id}` | Get venue details              |
| POST   | `/v1/admin/venues`      | Create a new venue             |
| PUT    | `/v1/admin/venues/{id}` | Update an existing venue       |
| DELETE | `/v1/admin/venues/{id}` | Delete a venue                 |

### Admin Users

| Method | Endpoint                        | Description                   |
| ------ | ------------------------------- | ----------------------------- |
| GET    | `/v1/admin/users`               | Get all users with pagination |
| GET    | `/v1/admin/users/{id}`          | Get user details by ID        |
| GET    | `/v1/admin/users/email/{email}` | Get user details by email     |
| GET    | `/v1/admin/users/name/{name}`   | Get user details by name      |

### Admin Bookings

| Method | Endpoint                                                           | Description                              |
| ------ | ------------------------------------------------------------------ | ---------------------------------------- |
| GET    | `/v1/admin/bookings`                                               | Get all bookings with pagination         |
| GET    | `/v1/admin/bookings/{id}`                                          | Get specific booking details             |
| GET    | `/v1/admin/bookings/venue/{venueId}`                               | Get bookings by venue ID                 |
| GET    | `/v1/admin/bookings/show/{showId}`                                 | Get bookings by show ID                  |
| GET    | `/v1/admin/bookings/event/{eventId}`                               | Get bookings by event ID                 |
| GET    | `/v1/admin/bookings/user/{userId}`                                 | Get bookings by user ID                  |
| GET    | `/v1/admin/bookings/venue/{venueId}/show/{showId}`                 | Get bookings by venue and show ID        |
| GET    | `/v1/admin/bookings/venue/{venueId}/event/{eventId}`               | Get bookings by venue and event ID       |
| GET    | `/v1/admin/bookings/show/{showId}/event/{eventId}`                 | Get bookings by show and event ID        |
| GET    | `/v1/admin/bookings/user/{userId}/venue/{venueId}`                 | Get bookings by user and venue ID        |
| GET    | `/v1/admin/bookings/user/{userId}/event/{eventId}`                 | Get bookings by user and event ID        |
| GET    | `/v1/admin/bookings/venue/{venueId}/show/{showId}/event/{eventId}` | Get bookings by venue, show and event ID |
| GET    | `/v1/admin/bookings/user/{userId}/venue/{venueId}/event/{eventId}` | Get bookings by user, venue and event ID |
| PATCH  | `/v1/admin/bookings/{id}/status`                                   | Update booking status                    |
| DELETE | `/v1/admin/bookings/{id}`                                          | Delete a booking                         |

### Admin Tickets

| Method | Endpoint                 | Description                     |
| ------ | ------------------------ | ------------------------------- |
| GET    | `/v1/admin/tickets`      | Get all tickets with pagination |
| GET    | `/v1/admin/tickets/{id}` | Get ticket details              |
| PUT    | `/v1/admin/tickets/{id}` | Update ticket information       |

### Admin Payments

| Method | Endpoint                  | Description                      |
| ------ | ------------------------- | -------------------------------- |
| GET    | `/v1/admin/payments`      | Get all payments with pagination |
| GET    | `/v1/admin/payments/{id}` | Get payment details              |
| PUT    | `/v1/admin/payments/{id}` | Update payment status            |

### Admin Refunds

| Method | Endpoint                 | Description                     |
| ------ | ------------------------ | ------------------------------- |
| GET    | `/v1/admin/refunds`      | Get all refunds with pagination |
| GET    | `/v1/admin/refunds/{id}` | Get refund details              |
| PUT    | `/v1/admin/refunds/{id}` | Update refund status            |

## Common Response Formats`

### Paginated Response

```json
{
  "data": [...],
  "pagination": {
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10,
    "first": true,
    "last": false
  },
  "links": {
    "self": "http://localhost:8080/api/v1/events?page=0&size=10",
    "next": "http://localhost:8080/api/v1/events?page=1&size=10",
    "prev": null
  }
}
```

### Error Response

```json
{
  "message": "Error description",
  "success": false,
  "errors": [
    {
      "field": "fieldName",
      "message": "Field-specific error message"
    }
  ]
}
```

## Error Handling

The API uses standard HTTP status codes:

- **200 OK** - Request successful
- **201 Created** - Resource created successfully
- **400 Bad Request** - Invalid request data
- **401 Unauthorized** - Authentication required
- **403 Forbidden** - Insufficient permissions
- **404 Not Found** - Resource not found
- **409 Conflict** - Resource conflict (e.g., seat already booked)
- **422 Unprocessable Entity** - Validation errors
- **500 Internal Server Error** - Server error

### Common Error Scenarios

- **Seat Already Booked** - When trying to book an already reserved seat
- **Booking Expired** - When payment is not completed within the reservation window
- **Insufficient Permissions** - When user tries to access admin-only resources
- **Unauthorized Admin Access** - When admin endpoints are accessed without `X-Admin-User: true` header
- **Missing User ID** - When user endpoints (except auth) are accessed without `X-User-ID` header
- **Invalid User ID** - When user endpoints are accessed with invalid `X-User-ID` header format
- **Validation Errors** - When required fields are missing or invalid
- **Resource Not Found** - When requesting non-existent resources

## Key Features

### Booking Workflow

1. **Seat Selection** - Users can view available seats for a show
2. **Reservation** - Seats are temporarily reserved for 5 minutes
3. **Payment** - Users must complete payment within the reservation window
4. **Confirmation** - Successful payment confirms the booking
5. **Cancellation** - Users can cancel bookings and receive refunds

### Email Notifications

- **Booking Confirmation** - Sent when booking is successfully created
- **Cancellation Confirmation** - Sent when booking is cancelled
- **Async Processing** - Emails are processed every 10 minutes

### Redis Integration

- **Distributed Locking** - Prevents double-booking of seats
- **Reservation Management** - Temporary seat reservations
- **Transaction Processing** - Atomic booking operations

---

_This API specification provides a high-level overview of the Evently platform. For detailed request/response schemas and specific implementation details, refer to `api-spec-detailed.md` file._
