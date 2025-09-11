-- Creates the events table
-- Aligned with planned Event entity and BaseEntity timestamps
CREATE TABLE IF NOT EXISTS events (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    category TEXT,
    status TEXT NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_event_status CHECK (
        status IN ('CREATED', 'LIVE', 'CLOSED', 'CANCELLED')
    )
);