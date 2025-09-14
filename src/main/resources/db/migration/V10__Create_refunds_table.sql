-- Create refunds table
CREATE TABLE refunds (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for refunds table
CREATE INDEX idx_refunds_payment_id ON refunds(payment_id);

CREATE INDEX idx_refunds_booking_id ON refunds(booking_id);

CREATE INDEX idx_refunds_status ON refunds(status);

CREATE INDEX idx_refunds_amount ON refunds(amount);

CREATE INDEX idx_refunds_processed_at ON refunds(processed_at);

CREATE INDEX idx_refunds_created_at ON refunds(created_at);

-- Create composite indexes for common queries
CREATE INDEX idx_refunds_payment_status ON refunds(payment_id, status);

CREATE INDEX idx_refunds_booking_status ON refunds(booking_id, status);

CREATE INDEX idx_refunds_status_created_at ON refunds(status, created_at);

-- Add foreign key constraints
ALTER TABLE
    refunds
ADD
    CONSTRAINT fk_refunds_payment_id FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE;

ALTER TABLE
    refunds
ADD
    CONSTRAINT fk_refunds_booking_id FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE;

-- Add check constraint for status values
ALTER TABLE
    refunds
ADD
    CONSTRAINT chk_refunds_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED'));

-- Add check constraint for amount
ALTER TABLE
    refunds
ADD
    CONSTRAINT chk_refunds_amount CHECK (amount >= 0);