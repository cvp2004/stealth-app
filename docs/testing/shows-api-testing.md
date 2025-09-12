# Shows API Testing Guide

This document provides cURL commands to test all the Shows API endpoints with appropriate request bodies.

## Base URL

```
http://localhost:8080/api/v1/shows
```

## Prerequisites

- Ensure the application is running on localhost:8080
- Have at least one venue and event created in the database
- Adjust venue IDs and event IDs in the examples based on your data

---

## 1. Create a Show

### Basic Show Creation

```bash
curl -X POST http://localhost:8080/api/v1/shows \
  -H "Content-Type: application/json" \
  -d '{
    "venueId": 1,
    "eventId": 1,
    "startTimestamp": "2024-12-25T19:00:00Z",
    "durationMinutes": 180
  }'
```

### Show with Different Event

```bash
curl -X POST http://localhost:8080/api/v1/shows \
  -H "Content-Type: application/json" \
  -d '{
    "venueId": 1,
    "eventId": 2,
    "startTimestamp": "2024-12-26T20:00:00Z",
    "durationMinutes": 120
  }'
```

### Show with Different Venue

```bash
curl -X POST http://localhost:8080/api/v1/shows \
  -H "Content-Type: application/json" \
  -d '{
    "venueId": 2,
    "eventId": 1,
    "startTimestamp": "2024-12-27T18:00:00Z",
    "durationMinutes": 60
  }'
```

**Note**: Title and description are automatically inherited from the event specified by `eventId`.

---

## 2. Get Show by ID

### Get Specific Show

```bash
curl -X GET http://localhost:8080/api/v1/shows/1 \
  -H "Accept: application/json"
```

### Get Non-existent Show (404 Error)

```bash
curl -X GET http://localhost:8080/api/v1/shows/999 \
  -H "Accept: application/json"
```

---

## 3. Update Show

### Update Show Details

```bash
curl -X PUT http://localhost:8080/api/v1/shows/1 \
  -H "Content-Type: application/json" \
  -d '{
    "venueId": 1,
    "eventId": 1,
    "startTimestamp": "2024-12-25T19:30:00Z",
    "durationMinutes": 200
  }'
```

### Update Show with Different Event

```bash
curl -X PUT http://localhost:8080/api/v1/shows/1 \
  -H "Content-Type: application/json" \
  -d '{
    "venueId": 1,
    "eventId": 2,
    "startTimestamp": "2024-12-28T17:00:00Z",
    "durationMinutes": 90
  }'
```

**Note**: Title and description will be updated to match the new event.

---

## 4. Delete Show

### Delete Existing Show

```bash
curl -X DELETE http://localhost:8080/api/v1/shows/1 \
  -H "Accept: application/json"
```

### Delete Non-existent Show (404 Error)

```bash
curl -X DELETE http://localhost:8080/api/v1/shows/999 \
  -H "Accept: application/json"
```

---

## 5. List Shows (GET with Query Parameters)

### List All Shows (Default Pagination)

```bash
curl -X GET http://localhost:8080/api/v1/shows \
  -H "Accept: application/json"
```

### List Shows with Pagination

```bash
curl -X GET "http://localhost:8080/api/v1/shows?page=0&size=10" \
  -H "Accept: application/json"
```

### List Shows with Sorting

```bash
curl -X GET "http://localhost:8080/api/v1/shows?page=0&size=20&sort=startTimestamp,desc" \
  -H "Accept: application/json"
```

### List Shows with Different Sort Options

```bash
# Sort by title ascending
curl -X GET "http://localhost:8080/api/v1/shows?sort=title,asc" \
  -H "Accept: application/json"

# Sort by duration descending
curl -X GET "http://localhost:8080/api/v1/shows?sort=durationMinutes,desc" \
  -H "Accept: application/json"
```

---

## 6. List Shows with Filtering (GET with Request Body)

### Filter by Venue

```bash
curl -X GET http://localhost:8080/api/v1/shows?page=0&size=20 \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "venueId": 1
  }'
```

### Filter by Event

```bash
curl -X GET http://localhost:8080/api/v1/shows?page=0&size=20 \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "eventId": 1
  }'
```

### Filter by Venue and Event

```bash
curl -X GET http://localhost:8080/api/v1/shows?page=0&size=10 \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "venueId": 1,
    "eventId": 1
  }'
```

### Filter by Specific Date

```bash
curl -X GET http://localhost:8080/api/v1/shows?page=0&size=50 \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "date": "2024-12-25T00:00:00Z"
  }'
```

### Filter by Date Range

```bash
curl -X GET http://localhost:8080/api/v1/shows?page=0&size=50 \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "fromDate": "2024-12-01T00:00:00Z",
    "toDate": "2024-12-31T23:59:59Z"
  }'
```

### Complex Filtering (Venue + Date Range)

```bash
curl -X GET "http://localhost:8080/api/v1/shows?page=0&size=20&sort=startTimestamp,asc" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "venueId": 1,
    "fromDate": "2024-12-20T00:00:00Z",
    "toDate": "2024-12-30T23:59:59Z"
  }'
```

### Filter by Future Shows Only

```bash
curl -X GET http://localhost:8080/api/v1/shows?page=0&size=50 \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "fromDate": "2024-12-01T00:00:00Z"
  }'
```

---

## 7. Error Testing

### Invalid Request Body (Empty Filter)

```bash
curl -X GET http://localhost:8080/api/v1/shows?page=0&size=10 \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{}'
```

### Invalid Show Creation (Missing Required Fields)

```bash
curl -X POST http://localhost:8080/api/v1/shows \
  -H "Content-Type: application/json" \
  -d '{
    "venueId": 1
  }'
```

### Invalid Show Creation (Past Date)

```bash
curl -X POST http://localhost:8080/api/v1/shows \
  -H "Content-Type: application/json" \
  -d '{
    "venueId": 1,
    "eventId": 1,
    "startTimestamp": "2020-01-01T19:00:00Z",
    "durationMinutes": 120
  }'
```

### Invalid Show Creation (Invalid Duration)

```bash
curl -X POST http://localhost:8080/api/v1/shows \
  -H "Content-Type: application/json" \
  -d '{
    "venueId": 1,
    "eventId": 1,
    "startTimestamp": "2024-12-25T19:00:00Z",
    "durationMinutes": 0
  }'
```

### Invalid Show Creation (Negative Duration)

```bash
curl -X POST http://localhost:8080/api/v1/shows \
  -H "Content-Type: application/json" \
  -d '{
    "venueId": 1,
    "eventId": 1,
    "startTimestamp": "2024-12-25T19:00:00Z",
    "durationMinutes": -60
  }'
```

### Invalid Show Creation (Non-existent Event)

```bash
curl -X POST http://localhost:8080/api/v1/shows \
  -H "Content-Type: application/json" \
  -d '{
    "venueId": 1,
    "eventId": 999,
    "startTimestamp": "2024-12-25T19:00:00Z",
    "durationMinutes": 120
  }'
```

### Invalid Filter (Both Date and Date Range)

```bash
curl -X GET http://localhost:8080/api/v1/shows?page=0&size=10 \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "date": "2024-12-25T00:00:00Z",
    "fromDate": "2024-12-01T00:00:00Z",
    "toDate": "2024-12-31T23:59:59Z"
  }'
```

---

## 8. Performance Testing

### Large Page Size

```bash
curl -X GET "http://localhost:8080/api/v1/shows?page=0&size=100" \
  -H "Accept: application/json"
```

### Multiple Page Requests

```bash
# Page 0
curl -X GET "http://localhost:8080/api/v1/shows?page=0&size=10" \
  -H "Accept: application/json"

# Page 1
curl -X GET "http://localhost:8080/api/v1/shows?page=1&size=10" \
  -H "Accept: application/json"

# Page 2
curl -X GET "http://localhost:8080/api/v1/shows?page=2&size=10" \
  -H "Accept: application/json"
```

---

## 9. Complete Workflow Test

### Step 1: Create a Venue (if not exists)

```bash
curl -X POST http://localhost:8080/api/v1/venues \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Venue",
    "address": "123 Test Street, Test City"
  }'
```

### Step 2: Create an Event (if not exists)

```bash
curl -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Event",
    "description": "Test event for show testing",
    "category": "Test"
  }'
```

### Step 3: Create Multiple Shows

```bash
# Show 1
curl -X POST http://localhost:8080/api/v1/shows \
  -H "Content-Type: application/json" \
  -d '{
    "venueId": 1,
    "eventId": 1,
    "startTimestamp": "2024-12-25T10:00:00Z",
    "durationMinutes": 90
  }'

# Show 2
curl -X POST http://localhost:8080/api/v1/shows \
  -H "Content-Type: application/json" \
  -d '{
    "venueId": 1,
    "eventId": 1,
    "startTimestamp": "2024-12-25T14:00:00Z",
    "durationMinutes": 120
  }'

# Show 3 (with different event)
curl -X POST http://localhost:8080/api/v1/shows \
  -H "Content-Type: application/json" \
  -d '{
    "venueId": 1,
    "eventId": 2,
    "startTimestamp": "2024-12-25T19:00:00Z",
    "durationMinutes": 180
  }'
```

### Step 4: List All Shows

```bash
curl -X GET http://localhost:8080/api/v1/shows \
  -H "Accept: application/json"
```

### Step 5: Filter Shows by Event

```bash
curl -X GET http://localhost:8080/api/v1/shows?page=0&size=10 \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "eventId": 1
  }'
```

### Step 6: Filter Shows by Date

```bash
curl -X GET http://localhost:8080/api/v1/shows?page=0&size=10 \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "date": "2024-12-25T00:00:00Z"
  }'
```

### Step 7: Update a Show

```bash
curl -X PUT http://localhost:8080/api/v1/shows/1 \
  -H "Content-Type: application/json" \
  -d '{
    "venueId": 1,
    "eventId": 1,
    "startTimestamp": "2024-12-25T10:30:00Z",
    "durationMinutes": 100
  }'
```

**Note**: Title and description will be updated to match the event.

### Step 8: Get Updated Show

```bash
curl -X GET http://localhost:8080/api/v1/shows/1 \
  -H "Accept: application/json"
```

---

## Expected Response Formats

### Successful Show Creation (201)

```json
{
  "id": 1,
  "refId": "SHW000001",
  "title": "Concert Night",
  "description": "Amazing concert performance",
  "startTimestamp": "2024-12-25T19:00:00Z",
  "date": "2024-12-25",
  "startTime": "19:00:00",
  "durationMinutes": 180,
  "venue": {
    "id": 1,
    "name": "Test Venue",
    "address": "123 Test Street"
  },
  "event": {
    "id": 1,
    "title": "Test Event",
    "category": "Test"
  },
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:00:00Z"
}
```

### Paginated List Response (200)

```json
{
  "isPaginated": true,
  "content": [
    {
      "id": 1,
      "refId": "SHW000001",
      "title": "Concert Night",
      "description": "Amazing concert performance",
      "startTimestamp": "2024-12-25T19:00:00Z",
      "date": "2024-12-25",
      "startTime": "19:00:00",
      "durationMinutes": 180,
      "venue": {
        "id": 1,
        "name": "Test Venue",
        "address": "123 Test Street"
      },
      "event": {
        "id": 1,
        "title": "Test Event",
        "category": "Test"
      },
      "createdAt": "2024-01-01T10:00:00Z",
      "updatedAt": "2024-01-01T10:00:00Z"
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  },
  "sort": {
    "fields": [
      {
        "property": "startTimestamp",
        "direction": "asc"
      }
    ]
  },
  "links": {
    "self": "/api/v1/shows?page=0&size=20",
    "first": "/api/v1/shows?page=0&size=20",
    "last": "/api/v1/shows?page=0&size=20",
    "next": null,
    "prev": null
  }
}
```

### Error Response (400)

```json
{
  "timestamp": "2024-01-01T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "At least one filter field must be provided",
  "path": "/api/v1/shows",
  "details": [
    {
      "field": "hasAtLeastOneFilter",
      "message": "At least one filter field must be provided"
    }
  ]
}
```

---

## Notes

1. **Adjust IDs**: Replace venue IDs and event IDs with actual values from your database
2. **Date Format**: All timestamps should be in ISO 8601 format with UTC timezone
3. **Validation**: The API validates that startTimestamp is in the future and durationMinutes is positive
4. **Filtering**: When using request body for filtering, at least one filter field must be provided
5. **Pagination**: Default page size is 50, default sort is by startTimestamp ascending
6. **Time Conflicts**: The API prevents overlapping shows at the same venue

## Troubleshooting

- **404 Errors**: Ensure the show/venue/event ID exists
- **400 Errors**: Check request body format and validation rules
- **409 Errors**: Usually indicates time conflicts or duplicate titles
- **500 Errors**: Check server logs for detailed error information
