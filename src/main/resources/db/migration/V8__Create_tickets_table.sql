-- Create tickets table
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    show_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for tickets table
CREATE INDEX idx_tickets_booking_id ON tickets(booking_id);

CREATE INDEX idx_tickets_show_id ON tickets(show_id);

CREATE INDEX idx_tickets_seat_id ON tickets(seat_id);

CREATE INDEX idx_tickets_price ON tickets(price);

CREATE INDEX idx_tickets_created_at ON tickets(created_at);

CREATE INDEX idx_tickets_updated_at ON tickets(updated_at);

-- Create composite indexes for common queries
CREATE INDEX idx_tickets_booking_show ON tickets(booking_id, show_id);

CREATE INDEX idx_tickets_show_seat ON tickets(show_id, seat_id);

CREATE INDEX idx_tickets_booking_created_at ON tickets(booking_id, created_at);

CREATE INDEX idx_tickets_show_created_at ON tickets(show_id, created_at);

-- Create unique constraint to prevent duplicate seat bookings for same show
CREATE UNIQUE INDEX uk_ticket_show_seat ON tickets(show_id, seat_id);

-- Add foreign key constraints
ALTER TABLE
    tickets
ADD
    CONSTRAINT fk_tickets_booking_id FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE;

ALTER TABLE
    tickets
ADD
    CONSTRAINT fk_tickets_show_id FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE;

ALTER TABLE
    tickets
ADD
    CONSTRAINT fk_tickets_seat_id FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE;

-- Add check constraint for price
ALTER TABLE
    tickets
ADD
    CONSTRAINT chk_tickets_price CHECK (price >= 0);

-- Add comments for documentation
COMMENT ON TABLE tickets IS 'Stores individual ticket information';

COMMENT ON COLUMN tickets.id IS 'Primary key for ticket';

COMMENT ON COLUMN tickets.booking_id IS 'Foreign key reference to bookings table';

COMMENT ON COLUMN tickets.show_id IS 'Foreign key reference to shows table';

COMMENT ON COLUMN tickets.seat_id IS 'Foreign key reference to seats table';

COMMENT ON COLUMN tickets.price IS 'Price of the ticket';

COMMENT ON COLUMN tickets.created_at IS 'Timestamp when ticket was created';

COMMENT ON COLUMN tickets.updated_at IS 'Timestamp when ticket was last updated';