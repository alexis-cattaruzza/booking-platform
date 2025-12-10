-- Create audit_logs table for security audit trail
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    username VARCHAR(255),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100),
    resource_id VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    details JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for efficient querying
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_status ON audit_logs(status);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_type, resource_id);

-- Add comment for documentation
COMMENT ON TABLE audit_logs IS 'Audit trail for security-sensitive operations';
COMMENT ON COLUMN audit_logs.action IS 'Type of action performed (e.g., LOGIN, LOGOUT, PASSWORD_RESET)';
COMMENT ON COLUMN audit_logs.status IS 'Result of the action (SUCCESS, FAILURE, ERROR)';
COMMENT ON COLUMN audit_logs.details IS 'Additional context stored as JSON';
