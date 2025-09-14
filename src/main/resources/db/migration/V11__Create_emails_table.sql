-- Create emails table
CREATE TABLE emails (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    email_type VARCHAR(50) NOT NULL,
    email_subject VARCHAR(500) NOT NULL,
    email_body TEXT NOT NULL,
    email_sent BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP WITH TIME ZONE,
    error_message VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for emails table
CREATE INDEX idx_emails_user_id ON emails(user_id);

CREATE INDEX idx_emails_email_type ON emails(email_type);

CREATE INDEX idx_emails_email_sent ON emails(email_sent);

CREATE INDEX idx_emails_sent_at ON emails(sent_at);

CREATE INDEX idx_emails_created_at ON emails(created_at);

-- Create composite indexes for common queries
CREATE INDEX idx_emails_user_type ON emails(user_id, email_type);

CREATE INDEX idx_emails_sent_created ON emails(email_sent, created_at);

-- Add foreign key constraints
ALTER TABLE
    emails
ADD
    CONSTRAINT fk_emails_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add check constraint for email_type values
ALTER TABLE
    emails
ADD
    CONSTRAINT chk_emails_email_type CHECK (
        email_type IN (
            'CANCEL_BOOKING',
            'BOOKING_CONFIRMATION'
        )
    );