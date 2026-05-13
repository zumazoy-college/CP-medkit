-- Add refresh_token column to users table
ALTER TABLE users ADD COLUMN refresh_token VARCHAR(500);

-- Add index for faster lookup
CREATE INDEX idx_users_refresh_token ON users(refresh_token);
