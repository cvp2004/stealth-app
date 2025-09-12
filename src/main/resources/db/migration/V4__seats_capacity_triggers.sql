-- Create trigger functions and triggers to keep venues.capacity in sync with seats count
-- Increments on INSERT into seats; decrements on DELETE from seats
-- Function: after INSERT on seats -> venues.capacity = capacity + 1
CREATE
OR REPLACE FUNCTION seats_capacity_after_insert() RETURNS trigger AS $$ BEGIN
UPDATE
    venues
SET
    capacity = COALESCE(capacity, 0) + 1
WHERE
    id = NEW.venue_id;

RETURN NEW;

END;

$$ LANGUAGE plpgsql;

-- Function: after DELETE on seats -> venues.capacity = capacity - 1 (not below zero)
CREATE
OR REPLACE FUNCTION seats_capacity_after_delete() RETURNS trigger AS $$ BEGIN
UPDATE
    venues
SET
    capacity = GREATEST(COALESCE(capacity, 0) - 1, 0)
WHERE
    id = OLD.venue_id;

RETURN OLD;

END;

$$ LANGUAGE plpgsql;

-- Trigger: after insert on seats
DROP TRIGGER IF EXISTS trg_seats_after_insert_capacity ON seats;

CREATE TRIGGER trg_seats_after_insert_capacity
AFTER
INSERT
    ON seats FOR EACH ROW EXECUTE FUNCTION seats_capacity_after_insert();

-- Trigger: after delete on seats
DROP TRIGGER IF EXISTS trg_seats_after_delete_capacity ON seats;

CREATE TRIGGER trg_seats_after_delete_capacity
AFTER
    DELETE ON seats FOR EACH ROW EXECUTE FUNCTION seats_capacity_after_delete();