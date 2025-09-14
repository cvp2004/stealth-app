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
    city VARCHAR(100),
    state VARCHAR(50),
    country VARCHAR(50),
    capacity INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add unique constraint on venue name
ALTER TABLE
    venues
ADD
    CONSTRAINT uk_venues_name UNIQUE (name);

-- Add comments for documentation
COMMENT ON TABLE venues IS 'Stores venue information including name, address, and capacity';

COMMENT ON COLUMN venues.id IS 'Primary key for venue';

COMMENT ON COLUMN venues.name IS 'Unique name of the venue';

COMMENT ON COLUMN venues.address IS 'Physical address of the venue';

COMMENT ON COLUMN venues.city IS 'City where the venue is located';

COMMENT ON COLUMN venues.state IS 'State/Province where the venue is located';

COMMENT ON COLUMN venues.country IS 'Country where the venue is located';

COMMENT ON COLUMN venues.capacity IS 'Total seating capacity of the venue (auto-calculated)';

COMMENT ON COLUMN venues.created_at IS 'Timestamp when venue was created';

COMMENT ON COLUMN venues.updated_at IS 'Timestamp when venue was last updated';

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
    price DECIMAL(10, 2) NOT NULL DEFAULT 100.00,
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

-- Add check constraint for price
ALTER TABLE
    seats
ADD
    CONSTRAINT chk_seats_price CHECK (price >= 0);

-- Add comments for documentation
COMMENT ON TABLE seats IS 'Stores individual seat information for each venue';

COMMENT ON COLUMN seats.id IS 'Primary key for seat';

COMMENT ON COLUMN seats.venue_id IS 'Foreign key reference to venues table';

COMMENT ON COLUMN seats.section IS 'Section identifier (e.g., VIP, General, Balcony)';

COMMENT ON COLUMN seats.row IS 'Row identifier within the section';

COMMENT ON COLUMN seats.seat_number IS 'Seat number within the row';

COMMENT ON COLUMN seats.price IS 'Price of the seat in USD';

COMMENT ON COLUMN seats.created_at IS 'Timestamp when seat was created';

COMMENT ON COLUMN seats.updated_at IS 'Timestamp when seat was last updated';

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
        status IN ('CREATED', 'LIVE', 'CLOSED', 'CANCELLED')
    );

-- Add comments for documentation
COMMENT ON TABLE events IS 'Stores event information and metadata';

COMMENT ON COLUMN events.id IS 'Primary key for event';

COMMENT ON COLUMN events.title IS 'Unique title of the event';

COMMENT ON COLUMN events.description IS 'Detailed description of the event';

COMMENT ON COLUMN events.category IS 'Category of the event (e.g., CONCERT, SPORTS, THEATER)';

COMMENT ON COLUMN events.status IS 'Current status of the event';

COMMENT ON COLUMN events.created_at IS 'Timestamp when event was created';

COMMENT ON COLUMN events.updated_at IS 'Timestamp when event was last updated';

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

-- Add comments for documentation
COMMENT ON TABLE shows IS 'Stores show information linking events to venues with timing';

COMMENT ON COLUMN shows.id IS 'Primary key for show';

COMMENT ON COLUMN shows.venue_id IS 'Foreign key reference to venues table';

COMMENT ON COLUMN shows.event_id IS 'Foreign key reference to events table';

COMMENT ON COLUMN shows.start_timestamp IS 'Start time of the show';

COMMENT ON COLUMN shows.duration_minutes IS 'Duration of the show in minutes';

COMMENT ON COLUMN shows.status IS 'Current status of the show';

COMMENT ON COLUMN shows.created_at IS 'Timestamp when show was created';

COMMENT ON COLUMN shows.updated_at IS 'Timestamp when show was last updated';

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

-- Add comments for documentation
COMMENT ON TABLE users IS 'Stores user account information';

COMMENT ON COLUMN users.id IS 'Primary key for user';

COMMENT ON COLUMN users.full_name IS 'Full name of the user';

COMMENT ON COLUMN users.email IS 'Unique email address of the user';

COMMENT ON COLUMN users.password IS 'Hashed password of the user';

COMMENT ON COLUMN users.created_at IS 'Timestamp when user was created';

COMMENT ON COLUMN users.updated_at IS 'Timestamp when user was last updated';

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

-- Add comments for documentation
COMMENT ON TABLE bookings IS 'Stores booking information for users and shows';

COMMENT ON COLUMN bookings.id IS 'Primary key for booking';

COMMENT ON COLUMN bookings.user_id IS 'Foreign key reference to users table';

COMMENT ON COLUMN bookings.show_id IS 'Foreign key reference to shows table';

COMMENT ON COLUMN bookings.status IS 'Current status of the booking';

COMMENT ON COLUMN bookings.total_amount IS 'Total amount for the booking';

COMMENT ON COLUMN bookings.created_at IS 'Timestamp when booking was created';

COMMENT ON COLUMN bookings.updated_at IS 'Timestamp when booking was last updated';

-- =====================================================
-- 7. TICKETS TABLE
-- =====================================================
-- Stores individual ticket information
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    show_id BIGINT NOT NULL,
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

ALTER TABLE
    tickets
ADD
    CONSTRAINT fk_tickets_show_id FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE;

ALTER TABLE
    tickets
ADD
    CONSTRAINT fk_tickets_seat_id FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE;

-- Add check constraint for price
ALTER TABLE
    tickets
ADD
    CONSTRAINT chk_tickets_price CHECK (price >= 0);

-- Add comments for documentation
COMMENT ON TABLE tickets IS 'Stores individual ticket information';

COMMENT ON COLUMN tickets.id IS 'Primary key for ticket';

COMMENT ON COLUMN tickets.booking_id IS 'Foreign key reference to bookings table';

COMMENT ON COLUMN tickets.show_id IS 'Foreign key reference to shows table';

COMMENT ON COLUMN tickets.seat_id IS 'Foreign key reference to seats table';

COMMENT ON COLUMN tickets.price IS 'Price of the ticket';

COMMENT ON COLUMN tickets.created_at IS 'Timestamp when ticket was created';

COMMENT ON COLUMN tickets.updated_at IS 'Timestamp when ticket was last updated';

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

-- Add comments for documentation
COMMENT ON TABLE payments IS 'Stores payment information for bookings';

COMMENT ON COLUMN payments.id IS 'Primary key for payment';

COMMENT ON COLUMN payments.booking_id IS 'Foreign key reference to bookings table';

COMMENT ON COLUMN payments.amount IS 'Amount of the payment';

COMMENT ON COLUMN payments.status IS 'Status of the payment';

COMMENT ON COLUMN payments.created_at IS 'Timestamp when payment was created';

COMMENT ON COLUMN payments.updated_at IS 'Timestamp when payment was last updated';

-- =====================================================
-- 9. REFUNDS TABLE
-- =====================================================
-- Stores refund information
CREATE TABLE refunds (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
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

-- Add comments for documentation
COMMENT ON TABLE refunds IS 'Stores refund information for cancelled bookings';

COMMENT ON COLUMN refunds.id IS 'Primary key for refund';

COMMENT ON COLUMN refunds.payment_id IS 'Foreign key reference to payments table';

COMMENT ON COLUMN refunds.booking_id IS 'Foreign key reference to bookings table';

COMMENT ON COLUMN refunds.amount IS 'Amount of the refund';

COMMENT ON COLUMN refunds.status IS 'Status of the refund';

COMMENT ON COLUMN refunds.processed_at IS 'Timestamp when refund was processed';

COMMENT ON COLUMN refunds.created_at IS 'Timestamp when refund was created';

COMMENT ON COLUMN refunds.updated_at IS 'Timestamp when refund was last updated';

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

-- Add comments for documentation
COMMENT ON TABLE emails IS 'Stores email queue for asynchronous processing';

COMMENT ON COLUMN emails.id IS 'Primary key for email';

COMMENT ON COLUMN emails.user_id IS 'Foreign key reference to users table';

COMMENT ON COLUMN emails.email_type IS 'Type of email to be sent';

COMMENT ON COLUMN emails.email_subject IS 'Subject line of the email';

COMMENT ON COLUMN emails.email_body IS 'Body content of the email';

COMMENT ON COLUMN emails.email_sent IS 'Flag indicating if email has been sent';

COMMENT ON COLUMN emails.sent_at IS 'Timestamp when email was sent';

COMMENT ON COLUMN emails.error_message IS 'Error message if email sending failed';

COMMENT ON COLUMN emails.created_at IS 'Timestamp when email was created';

COMMENT ON COLUMN emails.updated_at IS 'Timestamp when email was last updated';

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