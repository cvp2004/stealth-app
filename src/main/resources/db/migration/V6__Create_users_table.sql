-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for users table
CREATE INDEX idx_users_email ON users(email);

CREATE INDEX idx_users_full_name ON users(full_name);

CREATE INDEX idx_users_created_at ON users(created_at);

CREATE INDEX idx_users_updated_at ON users(updated_at);

-- Add check constraint for email format (basic validation)
ALTER TABLE
    users
ADD
    CONSTRAINT chk_users_email_format CHECK (
        email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'
    );

-- Add check constraint for full name (not empty)
ALTER TABLE
    users
ADD
    CONSTRAINT chk_users_full_name CHECK (LENGTH(TRIM(full_name)) > 0);

-- Add check constraint for password (minimum length)
ALTER TABLE
    users
ADD
    CONSTRAINT chk_users_password CHECK (LENGTH(password) >= 8);

-- Add comments for documentation
COMMENT ON TABLE users IS 'Stores user account information';

COMMENT ON COLUMN users.id IS 'Primary key for user';

COMMENT ON COLUMN users.full_name IS 'Full name of the user';

COMMENT ON COLUMN users.email IS 'Unique email address of the user';

COMMENT ON COLUMN users.password IS 'Hashed password of the user';

COMMENT ON COLUMN users.created_at IS 'Timestamp when user was created';

COMMENT ON COLUMN users.updated_at IS 'Timestamp when user was last updated';