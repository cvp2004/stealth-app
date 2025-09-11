-- Creates the venues table
-- Aligned with docs/schema.md, with added updated_at to match the entity
CREATE TABLE IF NOT EXISTS venues (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    address TEXT,
    capacity INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);