-- Creates the shows table with event relationship
CREATE TABLE IF NOT EXISTS shows (
    id BIGSERIAL PRIMARY KEY,
    venue_id BIGINT NOT NULL REFERENCES venues(id) ON DELETE
    SET
        NULL,
        event_id BIGINT REFERENCES events(id) ON DELETE
    SET
        NULL,
        title TEXT NOT NULL,
        description TEXT,
        start_timestamp TIMESTAMPTZ NOT NULL,
        duration_minutes INTEGER NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
        CONSTRAINT chk_show_duration CHECK (duration_minutes > 0),
        CONSTRAINT uk_show_venue_timestamp UNIQUE (venue_id, start_timestamp)
);

-- Indexes for performance
CREATE INDEX idx_shows_venue_id ON shows(venue_id);

CREATE INDEX idx_shows_event_id ON shows(event_id);

CREATE INDEX idx_shows_start_timestamp ON shows(start_timestamp);

CREATE INDEX idx_shows_duration ON shows(duration_minutes);