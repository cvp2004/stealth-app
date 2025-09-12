# Venue API Testing Guide

This guide provides comprehensive cURL requests for testing all Venue API endpoints, including CRUD operations and seat management.

## Table of Contents

1. [Venue CRUD Operations](#1-venue-crud-operations)
2. [Seat Management](#2-seat-management)
3. [Error Testing](#3-error-testing)
4. [Complete Workflow](#4-complete-workflow)

---

## 1. Venue CRUD Operations

### Create a Venue

#### Basic Venue Creation

```bash
curl -X POST http://localhost:8080/api/v1/venues \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Grand Theater",
    "address": "123 Main Street, City, State 12345"
  }'
```

#### Venue with Minimal Data

```bash
curl -X POST http://localhost:8080/api/v1/venues \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Small Hall"
  }'
```

#### Large Venue

```bash
curl -X POST http://localhost:8080/api/v1/venues \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Convention Center",
    "address": "456 Convention Blvd, Downtown, State 54321"
  }'
```

### Get Venue by ID

#### Get Specific Venue

```bash
curl -X GET http://localhost:8080/api/v1/venues/1 \
  -H "Accept: application/json"
```

#### Get Non-existent Venue

```bash
curl -X GET http://localhost:8080/api/v1/venues/999 \
  -H "Accept: application/json"
```

### Update Venue

#### Update Venue Details

```bash
curl -X PUT http://localhost:8080/api/v1/venues/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Grand Theater",
    "address": "123 Updated Main Street, City, State 12345"
  }'
```

#### Update Venue Name Only

```bash
curl -X PUT http://localhost:8080/api/v1/venues/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Theater Name"
  }'
```

#### Update Venue Address Only

```bash
curl -X PUT http://localhost:8080/api/v1/venues/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Grand Theater",
    "address": "789 New Address, Different City, State 67890"
  }'
```

### List Venues

#### List All Venues (Default Pagination)

```bash
curl -X GET http://localhost:8080/api/v1/venues \
  -H "Accept: application/json"
```

#### List Venues with Pagination

```bash
curl -X GET http://localhost:8080/api/v1/venues?page=0&size=10 \
  -H "Accept: application/json"
```

#### List Venues with Sorting

```bash
curl -X GET http://localhost:8080/api/v1/venues?sort=name,asc \
  -H "Accept: application/json"
```

#### List Venues with Custom Pagination and Sorting

```bash
curl -X GET http://localhost:8080/api/v1/venues?page=1&size=5&sort=name,desc \
  -H "Accept: application/json"
```

### Delete Venue

#### Delete Specific Venue

```bash
curl -X DELETE http://localhost:8080/api/v1/venues/1 \
  -H "Accept: application/json"
```

#### Delete Non-existent Venue

```bash
curl -X DELETE http://localhost:8080/api/v1/venues/999 \
  -H "Accept: application/json"
```

---

## 2. Seat Management

### Create Seats for Venue

#### Simple Seat Map (Single Section)

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

#### Complex Seat Map (Multiple Sections)

```bash
curl -X POST http://localhost:8080/api/v1/venues/1/seats \
  -H "Content-Type: application/json" \
  -d '{
    "sections": [
      {
        "sectionId": "VIP",
        "rows": [
          {
            "rowId": "A",
            "seatCount": 5
          },
          {
            "rowId": "B",
            "seatCount": 5
          }
        ]
      },
      {
        "sectionId": "General",
        "rows": [
          {
            "rowId": "1",
            "seatCount": 20
          },
          {
            "rowId": "2",
            "seatCount": 20
          },
          {
            "rowId": "3",
            "seatCount": 20
          }
        ]
      }
    ]
  }'
```

#### Theater-style Seat Map

```bash
curl -X POST http://localhost:8080/api/v1/venues/1/seats \
  -H "Content-Type: application/json" \
  -d '{
    "sections": [
      {
        "sectionId": "Orchestra",
        "rows": [
          {
            "rowId": "A",
            "seatCount": 15
          },
          {
            "rowId": "B",
            "seatCount": 15
          },
          {
            "rowId": "C",
            "seatCount": 15
          }
        ]
      },
      {
        "sectionId": "Mezzanine",
        "rows": [
          {
            "rowId": "1",
            "seatCount": 12
          },
          {
            "rowId": "2",
            "seatCount": 12
          }
        ]
      },
      {
        "sectionId": "Balcony",
        "rows": [
          {
            "rowId": "1",
            "seatCount": 8
          },
          {
            "rowId": "2",
            "seatCount": 8
          }
        ]
      }
    ]
  }'
```

### Get Seat Map for Venue

#### Get Seat Map

```bash
curl -X GET http://localhost:8080/api/v1/venues/1/seats \
  -H "Accept: application/json"
```

#### Get Seat Map for Non-existent Venue

```bash
curl -X GET http://localhost:8080/api/v1/venues/999/seats \
  -H "Accept: application/json"
```

---

## 3. Error Testing

### Invalid Venue Creation

#### Missing Required Fields

```bash
curl -X POST http://localhost:8080/api/v1/venues \
  -H "Content-Type: application/json" \
  -d '{}'
```

#### Empty Name

```bash
curl -X POST http://localhost:8080/api/v1/venues \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "address": "123 Main Street"
  }'
```

#### Name Too Long

```bash
curl -X POST http://localhost:8080/api/v1/venues \
  -H "Content-Type: application/json" \
  -d '{
    "name": "This is a very long venue name that exceeds the maximum allowed length of 255 characters and should trigger a validation error because it is way too long and contains more characters than the system allows for venue names which should be kept short and concise for better user experience and database efficiency",
    "address": "123 Main Street"
  }'
```

#### Address Too Long

```bash
curl -X POST http://localhost:8080/api/v1/venues \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Venue",
    "address": "This is an extremely long address that exceeds the maximum allowed length of 1000 characters and should trigger a validation error because it contains way too many characters and goes beyond the reasonable limit for address fields which should be kept concise and manageable for better database performance and user experience. This address keeps going and going with unnecessary details that no real address would ever have and continues to exceed the character limit to test the validation properly."
  }'
```

### Invalid Seat Map Creation

#### Missing Sections

```bash
curl -X POST http://localhost:8080/api/v1/venues/1/seats \
  -H "Content-Type: application/json" \
  -d '{}'
```

#### Empty Sections Array

```bash
curl -X POST http://localhost:8080/api/v1/venues/1/seats \
  -H "Content-Type: application/json" \
  -d '{
    "sections": []
  }'
```

#### Missing Section ID

```bash
curl -X POST http://localhost:8080/api/v1/venues/1/seats \
  -H "Content-Type: application/json" \
  -d '{
    "sections": [
      {
        "rows": [
          {
            "rowId": "1",
            "seatCount": 10
          }
        ]
      }
    ]
  }'
```

#### Missing Rows

```bash
curl -X POST http://localhost:8080/api/v1/venues/1/seats \
  -H "Content-Type: application/json" \
  -d '{
    "sections": [
      {
        "sectionId": "A",
        "rows": []
      }
    ]
  }'
```

#### Invalid Seat Count

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
            "seatCount": 0
          }
        ]
      }
    ]
  }'
```

#### Section ID Too Long

```bash
curl -X POST http://localhost:8080/api/v1/venues/1/seats \
  -H "Content-Type: application/json" \
  -d '{
    "sections": [
      {
        "sectionId": "ThisIsAVeryLongSectionIdThatExceedsTheMaximumAllowedLength",
        "rows": [
          {
            "rowId": "1",
            "seatCount": 10
          }
        ]
      }
    ]
  }'
```

#### Row ID Too Long

```bash
curl -X POST http://localhost:8080/api/v1/venues/1/seats \
  -H "Content-Type: application/json" \
  -d '{
    "sections": [
      {
        "sectionId": "A",
        "rows": [
          {
            "rowId": "ThisIsAVeryLongRowIdThatExceedsTheMaximumAllowedLength",
            "seatCount": 10
          }
        ]
      }
    ]
  }'
```

---

## 4. Complete Workflow

### Step 1: Create Multiple Venues

```bash
# Venue 1
curl -X POST http://localhost:8080/api/v1/venues \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Grand Theater",
    "address": "123 Main Street, City, State 12345"
  }'

# Venue 2
curl -X POST http://localhost:8080/api/v1/venues \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Convention Center",
    "address": "456 Convention Blvd, Downtown, State 54321"
  }'

# Venue 3
curl -X POST http://localhost:8080/api/v1/venues \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Small Hall"
  }'
```

### Step 2: List All Venues

```bash
curl -X GET http://localhost:8080/api/v1/venues \
  -H "Accept: application/json"
```

### Step 3: Get Specific Venue

```bash
curl -X GET http://localhost:8080/api/v1/venues/1 \
  -H "Accept: application/json"
```

### Step 4: Create Seat Maps for Venues

```bash
# Create seats for Grand Theater
curl -X POST http://localhost:8080/api/v1/venues/1/seats \
  -H "Content-Type: application/json" \
  -d '{
    "sections": [
      {
        "sectionId": "Orchestra",
        "rows": [
          {
            "rowId": "A",
            "seatCount": 15
          },
          {
            "rowId": "B",
            "seatCount": 15
          }
        ]
      },
      {
        "sectionId": "Balcony",
        "rows": [
          {
            "rowId": "1",
            "seatCount": 10
          }
        ]
      }
    ]
  }'

# Create seats for Convention Center
curl -X POST http://localhost:8080/api/v1/venues/2/seats \
  -H "Content-Type: application/json" \
  -d '{
    "sections": [
      {
        "sectionId": "Main",
        "rows": [
          {
            "rowId": "1",
            "seatCount": 50
          },
          {
            "rowId": "2",
            "seatCount": 50
          },
          {
            "rowId": "3",
            "seatCount": 50
          }
        ]
      }
    ]
  }'
```

### Step 5: View Seat Maps

```bash
# View Grand Theater seats
curl -X GET http://localhost:8080/api/v1/venues/1/seats \
  -H "Accept: application/json"

# View Convention Center seats
curl -X GET http://localhost:8080/api/v1/venues/2/seats \
  -H "Accept: application/json"
```

### Step 6: Update Venue

```bash
curl -X PUT http://localhost:8080/api/v1/venues/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Grand Theater",
    "address": "123 Updated Main Street, City, State 12345"
  }'
```

### Step 7: Get Updated Venue

```bash
curl -X GET http://localhost:8080/api/v1/venues/1 \
  -H "Accept: application/json"
```

### Step 8: List Venues with Sorting

```bash
# Sort by name ascending
curl -X GET http://localhost:8080/api/v1/venues?sort=name,asc \
  -H "Accept: application/json"

# Sort by name descending
curl -X GET http://localhost:8080/api/v1/venues?sort=name,desc \
  -H "Accept: application/json"
```

### Step 9: Test Pagination

```bash
# First page
curl -X GET http://localhost:8080/api/v1/venues?page=0&size=2 \
  -H "Accept: application/json"

# Second page
curl -X GET http://localhost:8080/api/v1/venues?page=1&size=2 \
  -H "Accept: application/json"
```

### Step 10: Clean Up (Delete Venues)

```bash
# Delete venues (this will also delete associated seats)
curl -X DELETE http://localhost:8080/api/v1/venues/1 \
  -H "Accept: application/json"

curl -X DELETE http://localhost:8080/api/v1/venues/2 \
  -H "Accept: application/json"

curl -X DELETE http://localhost:8080/api/v1/venues/3 \
  -H "Accept: application/json"
```

---

## Expected Response Formats

### VenueResponse

```json
{
  "id": 1,
  "refId": "ven-000001",
  "name": "Grand Theater",
  "address": "123 Main Street, City, State 12345",
  "capacity": 40
}
```

### PaginationResponse<VenueResponse>

```json
{
  "data": [
    {
      "id": 1,
      "refId": "ven-000001",
      "name": "Grand Theater",
      "address": "123 Main Street, City, State 12345",
      "capacity": 40
    }
  ],
  "pageMeta": {
    "page": 0,
    "size": 50,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "sortMeta": {
    "sorted": true,
    "sortFields": [
      {
        "field": "name",
        "direction": "ASC"
      }
    ]
  },
  "links": {
    "self": "http://localhost:8080/api/v1/venues?page=0&size=50&sort=name,asc",
    "first": "http://localhost:8080/api/v1/venues?page=0&size=50&sort=name,asc",
    "last": "http://localhost:8080/api/v1/venues?page=0&size=50&sort=name,asc"
  }
}
```

### BulkSeatCreationResponse

```json
{
  "seatCount": 40,
  "sectionCount": 2,
  "venueName": "Grand Theater",
  "totalCapacity": 40
}
```

### SeatMapResponse

```json
{
  "venueName": "Grand Theater",
  "totalCapacity": 40,
  "sections": [
    {
      "sectionId": "Orchestra",
      "rows": [
        {
          "rowId": "A",
          "seats": [
            {
              "id": 1,
              "refId": "st-000001",
              "sectionId": "Orchestra",
              "rowId": "A",
              "seatNumber": 1,
              "status": "AVAILABLE"
            }
          ]
        }
      ]
    }
  ]
}
```

---

## Notes

- All timestamps are in ISO format (UTC)
- Reference IDs are auto-generated with prefixes (ven- for venues, st- for seats)
- Venue capacity is automatically calculated when seats are created
- Deleting a venue will cascade delete all associated seats
- Pagination defaults: page=0, size=50, sort=name,asc
- All endpoints return appropriate HTTP status codes
- Validation errors return 400 Bad Request with detailed error messages
- Not found errors return 404 Not Found
- Successful creation returns 201 Created
- Successful updates return 200 OK
- Successful deletions return 204 No Content
