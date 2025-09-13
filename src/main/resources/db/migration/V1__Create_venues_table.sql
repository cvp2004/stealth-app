-- Create venues table
CREATE TABLE venues (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(500),
    capacity INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for venues table
CREATE INDEX idx_venues_name ON venues(name);

CREATE INDEX idx_venues_created_at ON venues(created_at);

CREATE INDEX idx_venues_updated_at ON venues(updated_at);

-- Create unique constraint on venue name
ALTER TABLE
    venues
ADD
    CONSTRAINT uk_venues_name UNIQUE (name);

-- Add comments for documentation
COMMENT ON TABLE venues IS 'Stores venue information including name, address, and capacity';

COMMENT ON COLUMN venues.id IS 'Primary key for venue';

COMMENT ON COLUMN venues.name IS 'Unique name of the venue';

COMMENT ON COLUMN venues.address IS 'Physical address of the venue';

COMMENT ON COLUMN venues.capacity IS 'Total seating capacity of the venue (auto-calculated)';

COMMENT ON COLUMN venues.created_at IS 'Timestamp when venue was created';

COMMENT ON COLUMN venues.updated_at IS 'Timestamp when venue was last updated';