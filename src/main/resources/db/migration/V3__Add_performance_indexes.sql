-- Additional performance indexes for common query patterns
-- Index for counting seats by venue (used in capacity calculation)
CREATE INDEX idx_seats_venue_count ON seats(venue_id)
WHERE venue_id IS NOT NULL;

-- Index for seat ordering (used in getSeatMap queries)
CREATE INDEX idx_seats_venue_order ON seats(venue_id, section, row, seat_number) INCLUDE (id);
