-- Create bookings table
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    show_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    total_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for bookings table
CREATE INDEX idx_bookings_user_id ON bookings(user_id);

CREATE INDEX idx_bookings_show_id ON bookings(show_id);

CREATE INDEX idx_bookings_status ON bookings(status);

CREATE INDEX idx_bookings_total_amount ON bookings(total_amount);

CREATE INDEX idx_bookings_created_at ON bookings(created_at);

CREATE INDEX idx_bookings_updated_at ON bookings(updated_at);

-- Create composite indexes for common queries
CREATE INDEX idx_bookings_user_status ON bookings(user_id, status);

CREATE INDEX idx_bookings_show_status ON bookings(show_id, status);

CREATE INDEX idx_bookings_user_created_at ON bookings(user_id, created_at);

CREATE INDEX idx_bookings_show_created_at ON bookings(show_id, created_at);

CREATE INDEX idx_bookings_status_created_at ON bookings(status, created_at);

-- Add foreign key constraints
ALTER TABLE
    bookings
ADD
    CONSTRAINT fk_bookings_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE
    bookings
ADD
    CONSTRAINT fk_bookings_show_id FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE;

-- Add check constraint for status values
ALTER TABLE
    bookings
ADD
    CONSTRAINT chk_bookings_status CHECK (
        status IN ('CONFIRMED', 'CANCELLED', 'WAITLISTED')
    );

-- Add check constraint for total amount
ALTER TABLE
    bookings
ADD
    CONSTRAINT chk_bookings_total_amount CHECK (total_amount >= 0);

-- Add comments for documentation
COMMENT ON TABLE bookings IS 'Stores booking information for users and shows';

COMMENT ON COLUMN bookings.id IS 'Primary key for booking';

COMMENT ON COLUMN bookings.user_id IS 'Foreign key reference to users table';

COMMENT ON COLUMN bookings.show_id IS 'Foreign key reference to shows table';

COMMENT ON COLUMN bookings.status IS 'Current status of the booking';

COMMENT ON COLUMN bookings.total_amount IS 'Total amount for the booking';

COMMENT ON COLUMN bookings.created_at IS 'Timestamp when booking was created';

COMMENT ON COLUMN bookings.updated_at IS 'Timestamp when booking was last updated';