-- ============================================
-- Phase 3 Security: Password Reset
-- ============================================

-- ==================================
-- USERS TABLE: Password Reset Fields
-- ==================================

-- Add password reset fields to users table
ALTER TABLE users ADD COLUMN password_reset_token VARCHAR(64);
ALTER TABLE users ADD COLUMN password_reset_token_expires_at TIMESTAMP;

-- Create index on password_reset_token for fast lookups
CREATE INDEX idx_users_password_reset_token ON users(password_reset_token);

-- Add comments for documentation
COMMENT ON COLUMN users.password_reset_token IS 'Token sent via email for password reset (UUID)';
COMMENT ON COLUMN users.password_reset_token_expires_at IS 'Expiration timestamp for password reset token (1 hour validity)';
