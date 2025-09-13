-- Create seats table
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

-- Create indexes for seats table
CREATE INDEX idx_seats_venue_id ON seats(venue_id);

CREATE INDEX idx_seats_section ON seats(section);

CREATE INDEX idx_seats_row ON seats(row);

CREATE INDEX idx_seats_created_at ON seats(created_at);

CREATE INDEX idx_seats_updated_at ON seats(updated_at);

-- Create composite indexes for common queries
CREATE INDEX idx_seats_venue_section ON seats(venue_id, section);

CREATE INDEX idx_seats_venue_section_row ON seats(venue_id, section, row);

CREATE INDEX idx_seats_venue_section_row_seat ON seats(venue_id, section, row, seat_number);

-- Create unique constraint to prevent duplicate seats
ALTER TABLE
    seats
ADD
    CONSTRAINT uk_seat_unique UNIQUE (venue_id, section, row, seat_number);

-- Add comments for documentation
COMMENT ON TABLE seats IS 'Stores individual seat information for each venue';

COMMENT ON COLUMN seats.id IS 'Primary key for seat';

COMMENT ON COLUMN seats.venue_id IS 'Foreign key reference to venues table';

COMMENT ON COLUMN seats.section IS 'Section identifier (e.g., VIP, General, Balcony)';

COMMENT ON COLUMN seats.row IS 'Row identifier within the section';

COMMENT ON COLUMN seats.seat_number IS 'Seat number within the row';

COMMENT ON COLUMN seats.created_at IS 'Timestamp when seat was created';

COMMENT ON COLUMN seats.updated_at IS 'Timestamp when seat was last updated';