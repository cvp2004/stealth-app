-- Create payments table
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for payments table
CREATE INDEX idx_payments_booking_id ON payments(booking_id);

CREATE INDEX idx_payments_amount ON payments(amount);

CREATE INDEX idx_payments_status ON payments(status);

CREATE INDEX idx_payments_created_at ON payments(created_at);

CREATE INDEX idx_payments_updated_at ON payments(updated_at);

-- Create composite indexes for common queries
CREATE INDEX idx_payments_booking_status ON payments(booking_id, status);

CREATE INDEX idx_payments_status_created_at ON payments(status, created_at);

CREATE INDEX idx_payments_booking_created_at ON payments(booking_id, created_at);

-- Add foreign key constraints
ALTER TABLE
    payments
ADD
    CONSTRAINT fk_payments_booking_id FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE;

-- Add check constraint for status values
ALTER TABLE
    payments
ADD
    CONSTRAINT chk_payments_status CHECK (status IN ('SUCCESS', 'FAILED'));

-- Add check constraint for amount
ALTER TABLE
    payments
ADD
    CONSTRAINT chk_payments_amount CHECK (amount >= 0);

-- Add comments for documentation
COMMENT ON TABLE payments IS 'Stores payment information for bookings';

COMMENT ON COLUMN payments.id IS 'Primary key for payment';

COMMENT ON COLUMN payments.booking_id IS 'Foreign key reference to bookings table';

COMMENT ON COLUMN payments.amount IS 'Amount of the payment';

COMMENT ON COLUMN payments.status IS 'Status of the payment';

COMMENT ON COLUMN payments.created_at IS 'Timestamp when payment was created';

COMMENT ON COLUMN payments.updated_at IS 'Timestamp when payment was last updated';