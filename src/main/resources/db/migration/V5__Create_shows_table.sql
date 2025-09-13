-- Create shows table
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

-- Create indexes for shows table
CREATE INDEX idx_shows_venue_id ON shows(venue_id);

CREATE INDEX idx_shows_event_id ON shows(event_id);

CREATE INDEX idx_shows_start_timestamp ON shows(start_timestamp);

CREATE INDEX idx_shows_status ON shows(status);

CREATE INDEX idx_shows_created_at ON shows(created_at);

-- Create composite indexes for common queries
CREATE INDEX idx_shows_venue_start_timestamp ON shows(venue_id, start_timestamp);

CREATE INDEX idx_shows_event_start_timestamp ON shows(event_id, start_timestamp);

CREATE INDEX idx_shows_venue_event ON shows(venue_id, event_id);

CREATE INDEX idx_shows_status_start_timestamp ON shows(status, start_timestamp);

-- Note: Date-based queries will use the existing start_timestamp index
-- which is efficient for range queries on dates
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