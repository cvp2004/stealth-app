-- Creates the seats table
-- Aligned with model Seat.java and docs/schema.md; includes timestamps to match BaseEntity
CREATE TABLE IF NOT EXISTS seats (
    id BIGSERIAL PRIMARY KEY,
    venue_id BIGINT NOT NULL REFERENCES venues(id),
    section TEXT,
    row TEXT,
    seat_number TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_seat_unique UNIQUE (venue_id, section, row, seat_number)
);