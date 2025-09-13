-- Create events table
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    category VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for events table
CREATE INDEX idx_events_title ON events(title);

CREATE INDEX idx_events_created_at ON events(created_at);

-- Create unique constraint on event title
ALTER TABLE
    events
ADD
    CONSTRAINT uk_events_title UNIQUE (title);

-- Add check constraint for status values
ALTER TABLE
    events
ADD
    CONSTRAINT chk_events_status CHECK (status IN ('CREATED', 'LIVE', 'CLOSED'));