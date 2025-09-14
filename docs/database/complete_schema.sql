-- =====================================================
-- Evently Platform - Complete Database Schema
-- =====================================================
-- This file contains the complete database schema for the Evently platform
-- Generated from migration files V1 through V11
-- Excludes indexes for cleaner schema overview
-- =====================================================
-- =====================================================
-- 1. VENUES TABLE
-- =====================================================
-- Stores venue information including name, address, and capacity
CREATE TABLE venues (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(500),
    capacity INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add unique constraint on venue name
ALTER TABLE
    venues
ADD
    CONSTRAINT uk_venues_name UNIQUE (name);

-- =====================================================
-- 2. SEATS TABLE
-- =====================================================
-- Stores individual seat information for each venue
CREATE TABLE seats (
    id BIGSERIAL PRIMARY KEY,
    venue_id BIGINT NOT NULL,
    section VARCHAR(50),
    row VARCHAR(10),
    seat_number VARCHAR(10),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Foreign key constraint
    CONSTRAINT fk_seats_venue_id FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE
);

-- Create unique constraint to prevent duplicate seats
ALTER TABLE
    seats
ADD
    CONSTRAINT uk_seat_unique UNIQUE (venue_id, section, row, seat_number);

--
-- =====================================================
-- 3. EVENTS TABLE
-- =====================================================
-- Stores event information
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    category VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create unique constraint on event title
ALTER TABLE
    events
ADD
    CONSTRAINT uk_events_title UNIQUE (title);

-- Add check constraint for status values
ALTER TABLE
    events
ADD
    CONSTRAINT chk_events_status CHECK (
        status IN ('CREATED', 'LIVE', 'CLOSED')
    );

-- =====================================================
-- 4. SHOWS TABLE
-- =====================================================
-- Stores show information linking events to venues with timing
CREATE TABLE shows (
    id BIGSERIAL PRIMARY KEY,
    venue_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    start_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_minutes INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'LIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraints
ALTER TABLE
    shows
ADD
    CONSTRAINT fk_shows_venue_id FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE;

ALTER TABLE
    shows
ADD
    CONSTRAINT fk_shows_event_id FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE;

-- Add check constraint for status values
ALTER TABLE
    shows
ADD
    CONSTRAINT chk_shows_status CHECK (status IN ('LIVE', 'CLOSED', 'CANCELLED'));

-- Add check constraint for duration
ALTER TABLE
    shows
ADD
    CONSTRAINT chk_shows_duration CHECK (
        duration_minutes > 0
        AND duration_minutes <= 1440
    );

-- =====================================================
-- 5. USERS TABLE
-- =====================================================
-- Stores user account information
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add check constraint for email format (basic validation)
ALTER TABLE
    users
ADD
    CONSTRAINT chk_users_email_format CHECK (
        email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'
    );

-- Add check constraint for full name (not empty)
ALTER TABLE
    users
ADD
    CONSTRAINT chk_users_full_name CHECK (LENGTH(TRIM(full_name)) > 0);

-- Add check constraint for password (minimum length)
ALTER TABLE
    users
ADD
    CONSTRAINT chk_users_password CHECK (LENGTH(password) >= 8);

-- =====================================================
-- 6. BOOKINGS TABLE
-- =====================================================
-- Stores booking information
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    show_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    total_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraints
ALTER TABLE
    bookings
ADD
    CONSTRAINT fk_bookings_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE
    bookings
ADD
    CONSTRAINT fk_bookings_show_id FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE;

-- Add check constraint for status values
ALTER TABLE
    bookings
ADD
    CONSTRAINT chk_bookings_status CHECK (
        status IN ('CONFIRMED', 'CANCELLED', 'WAITLISTED')
    );

-- Add check constraint for total amount
ALTER TABLE
    bookings
ADD
    CONSTRAINT chk_bookings_total_amount CHECK (total_amount >= 0);

-- =====================================================
-- 7. TICKETS TABLE
-- =====================================================
-- Stores individual ticket information
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraints
ALTER TABLE
    tickets
ADD
    CONSTRAINT fk_tickets_booking_id FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE;

--
ALTER TABLE
    tickets
ADD
    CONSTRAINT fk_tickets_seat_id FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE;

-- Add check constraint for price
ALTER TABLE
    tickets
ADD
    CONSTRAINT chk_tickets_price CHECK (price >= 0);

-- =====================================================
-- 8. PAYMENTS TABLE
-- =====================================================
-- Stores payment information
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraints
ALTER TABLE
    payments
ADD
    CONSTRAINT fk_payments_booking_id FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE;

-- Add check constraint for status values
ALTER TABLE
    payments
ADD
    CONSTRAINT chk_payments_status CHECK (status IN ('SUCCESS', 'FAILED'));

-- Add check constraint for amount
ALTER TABLE
    payments
ADD
    CONSTRAINT chk_payments_amount CHECK (amount >= 0);

-- =====================================================
-- 9. REFUNDS TABLE
-- =====================================================
-- Stores refund information
CREATE TABLE refunds (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraints
ALTER TABLE
    refunds
ADD
    CONSTRAINT fk_refunds_payment_id FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE;

ALTER TABLE
    refunds
ADD
    CONSTRAINT fk_refunds_booking_id FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE;

-- Add check constraint for status values
ALTER TABLE
    refunds
ADD
    CONSTRAINT chk_refunds_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED'));

-- Add check constraint for amount
ALTER TABLE
    refunds
ADD
    CONSTRAINT chk_refunds_amount CHECK (amount >= 0);

-- =====================================================
-- 10. EMAILS TABLE
-- =====================================================
-- Stores email queue for asynchronous processing
CREATE TABLE emails (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    email_type VARCHAR(50) NOT NULL,
    email_subject VARCHAR(500) NOT NULL,
    email_body TEXT NOT NULL,
    email_sent BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP WITH TIME ZONE,
    error_message VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraints
ALTER TABLE
    emails
ADD
    CONSTRAINT fk_emails_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add check constraint for email_type values
ALTER TABLE
    emails
ADD
    CONSTRAINT chk_emails_email_type CHECK (
        email_type IN ('CANCEL_BOOKING', 'BOOKING_CONFIRMATION')
    );

-- =====================================================
-- SCHEMA SUMMARY
-- =====================================================
-- This schema supports the following core functionality:
-- 1. Venue management with seat mapping
-- 2. Event and show scheduling
-- 3. User account management
-- 4. Booking and ticket management
-- 5. Payment processing
-- 6. Refund handling
-- 7. Asynchronous email notifications
-- 
-- Key relationships:
-- - Venues have many seats
-- - Events have many shows (at different venues/times)
-- - Users make bookings for shows
-- - Bookings contain multiple tickets (seats)
-- - Payments are linked to bookings
-- - Refunds are linked to payments and bookings
-- - Emails are queued for users
-- =====================================================