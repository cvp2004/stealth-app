# Evently Database Schema

> **Event Ticket Management System Database Schema Documentation**

This document outlines the complete database schema for the Evently event ticket management system, including all tables, relationships, and constraints.

## 📋 Table of Contents

### Database Tables

- [Users](#users)
- [Venues](#venues)
- [Shows](#shows)
- [Seats](#seats)
- [Show Seats](#show-seats)
- [Bookings](#bookings)
- [Booked Seats](#booked-seats)
- [Attendees](#attendees)
- [Payments](#payments)
- [Refunds](#refunds)

### Documentation Sections

- [Data Types Reference](#data-types-reference)
- [Technical Notes](#technical-notes)

---

## Data Types Reference

| Data Type       | Description                                          | Usage                                      |
| --------------- | ---------------------------------------------------- | ------------------------------------------ |
| `BIGSERIAL`     | Auto-incrementing 64-bit integer                     | Primary keys for all tables                |
| `BIGINT`        | 64-bit signed integer                                | Foreign key references                     |
| `TEXT`          | Variable-length character string                     | Names, descriptions, addresses, emails     |
| `INT`           | 32-bit signed integer                                | Capacity, age, seat numbers                |
| `DECIMAL(10,2)` | Fixed-point decimal with 10 digits, 2 decimal places | Monetary amounts (prices, payments)        |
| `TIMESTAMPTZ`   | Timestamp with timezone                              | Created dates, event times, check-in times |
| `JSONB`         | Binary JSON data                                     | Special needs, flexible data storage       |

---

## Technical Notes

- All tables use `BIGSERIAL` for primary keys to support high-volume operations
- Timestamps use `TIMESTAMPTZ` for timezone-aware storage
- Foreign key constraints ensure referential integrity
- Unique constraints prevent data duplication where appropriate
- Cascade deletes maintain data consistency in related tables

---

## Database Tables

### Users

Stores user account information for the platform.

```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  full_name TEXT NOT NULL,
  email TEXT UNIQUE NOT NULL,
  phone TEXT,
  created_at TIMESTAMPTZ DEFAULT now()
);
```

---

### Venues

Manages venue information and capacity details.

```sql
CREATE TABLE venues (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  address TEXT,
  capacity INT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now()
);
```

---

### Shows

Defines events and their scheduling information.

```sql
CREATE TABLE shows (
  id BIGSERIAL PRIMARY KEY,
  venue_id BIGINT NOT NULL REFERENCES venues(id),
  title TEXT NOT NULL,
  description TEXT,
  start_time TIMESTAMPTZ NOT NULL,
  end_time TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now()
);
```

---

### Seats

Defines individual seat locations within venues.

```sql
CREATE TABLE seats (
  id BIGSERIAL PRIMARY KEY,
  venue_id BIGINT NOT NULL REFERENCES venues(id),
  section TEXT,
  row TEXT,
  seat_number TEXT,
  UNIQUE (venue_id, section, row, seat_number)
);
```

---

### Show Seats

Links seats to specific shows with pricing and availability.

```sql
CREATE TABLE show_seats (
  id BIGSERIAL PRIMARY KEY,
  show_id BIGINT NOT NULL REFERENCES shows(id),
  seat_id BIGINT NOT NULL REFERENCES seats(id),
  price DECIMAL(10,2) NOT NULL,
  status TEXT NOT NULL DEFAULT 'AVAILABLE'
    CHECK (status IN ('AVAILABLE', 'BOOKED')),
  UNIQUE (show_id, seat_id)
);
```

---

### Bookings

Manages user booking transactions and status.

```sql
CREATE TABLE bookings (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  show_id BIGINT NOT NULL REFERENCES shows(id),
  status TEXT NOT NULL DEFAULT 'CONFIRMED'
    CHECK (status IN ('CONFIRMED', 'CANCELLED', 'REFUNDED', 'WAITLISTED', 'PARTIALLY_ALLOCATED')),
  total_amount DECIMAL(10,2) NOT NULL,
  payment_id BIGINT REFERENCES payments(id), -- FK to payments
  created_at TIMESTAMPTZ DEFAULT now()
);
```

---

### Booked Seats

Records specific seat assignments for bookings.

```sql
CREATE TABLE booked_seats (
  id BIGSERIAL PRIMARY KEY,
  booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
  show_seat_id BIGINT NOT NULL REFERENCES show_seats(id),
  price DECIMAL(10,2) NOT NULL,
  UNIQUE (show_seat_id)  -- prevents double booking
);
```

---

### Attendees

Stores detailed attendee information for each booked seat.

```sql
CREATE TABLE attendees (
  id BIGSERIAL PRIMARY KEY,
  booked_seat_id BIGINT NOT NULL UNIQUE REFERENCES booked_seats(id) ON DELETE CASCADE,
  full_name TEXT NOT NULL,
  email TEXT,
  phone TEXT,
  age INT,
  government_id TEXT,
  special_needs JSONB,
  qr_code TEXT UNIQUE,  -- for check-in
  checked_in_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT now()
);
```

---

### Payments

Manages payment transactions and provider integration.

```sql
CREATE TABLE payments (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  show_id BIGINT NOT NULL REFERENCES shows(id),
  amount DECIMAL(10,2) NOT NULL,
  status TEXT NOT NULL DEFAULT 'PENDING'
    CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED', 'PARTIALLY_REFUNDED')),
  provider TEXT,         -- e.g., Razorpay, Stripe, PayPal
  provider_payment_id TEXT UNIQUE, -- external reference
  created_at TIMESTAMPTZ DEFAULT now()
);
```

---

### Refunds

Tracks refund transactions and processing status.

```sql
CREATE TABLE refunds (
  id BIGSERIAL PRIMARY KEY,
  payment_id BIGINT NOT NULL REFERENCES payments(id),
  booking_id BIGINT NOT NULL REFERENCES bookings(id),
  amount DECIMAL(10,2) NOT NULL,
  status TEXT NOT NULL DEFAULT 'PENDING'
    CHECK (status IN ('PENDING', 'PROCESSED', 'FAILED')),
  processed_at TIMESTAMPTZ
);
```

---
