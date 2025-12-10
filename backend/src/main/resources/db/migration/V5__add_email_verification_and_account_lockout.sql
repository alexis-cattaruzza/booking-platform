-- ============================================
-- Phase 2 Security: Email Verification, Account Lockout & Cancellation Token Expiration
-- ============================================

-- ==================================
-- USERS TABLE: Email Verification & Account Lockout
-- ==================================

-- Add email verification fields to users table
ALTER TABLE users ADD COLUMN email_verification_token VARCHAR(64);
ALTER TABLE users ADD COLUMN email_verification_token_expires_at TIMESTAMP;

-- Add account lockout fields to users table
ALTER TABLE users ADD COLUMN failed_login_attempts INTEGER DEFAULT 0;
ALTER TABLE users ADD COLUMN account_locked_until TIMESTAMP;

-- Create index on email_verification_token for fast lookups
CREATE INDEX idx_users_email_verification_token ON users(email_verification_token);

-- Add comments for documentation
COMMENT ON COLUMN users.email_verification_token IS 'Token sent via email for email verification (UUID)';
COMMENT ON COLUMN users.email_verification_token_expires_at IS 'Expiration timestamp for email verification token (24h validity)';
COMMENT ON COLUMN users.failed_login_attempts IS 'Counter for failed login attempts (reset on successful login)';
COMMENT ON COLUMN users.account_locked_until IS 'Timestamp until which the account is locked (15 minutes after 5 failed attempts)';

-- ==================================
-- APPOINTMENTS TABLE: Cancellation Token Expiration
-- ==================================

-- Add cancellation token expiration (24 hours before appointment)
ALTER TABLE appointments ADD COLUMN cancellation_token_expires_at TIMESTAMP;

-- Create index for faster expiration checks
CREATE INDEX idx_appointments_cancellation_token_expires ON appointments(cancellation_token_expires_at);

-- Add comment for documentation
COMMENT ON COLUMN appointments.cancellation_token_expires_at IS 'Expiration timestamp for cancellation token (24 hours before appointment datetime)';
