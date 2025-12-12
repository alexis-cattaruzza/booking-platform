-- Create business_holidays table for vacation/holiday blocking
CREATE TABLE IF NOT EXISTS business_holidays (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_business_holidays_business
        FOREIGN KEY (business_id)
        REFERENCES businesses(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_holiday_dates
        CHECK (end_date >= start_date)
);

-- Create indexes for efficient queries
CREATE INDEX idx_business_holidays_business_id ON business_holidays(business_id);
CREATE INDEX idx_business_holidays_dates ON business_holidays(start_date, end_date);

-- Add comment
COMMENT ON TABLE business_holidays IS 'Stores vacation/holiday periods when business is unavailable for bookings';
COMMENT ON COLUMN business_holidays.start_date IS 'First day of vacation (inclusive)';
COMMENT ON COLUMN business_holidays.end_date IS 'Last day of vacation (inclusive)';
COMMENT ON COLUMN business_holidays.reason IS 'Optional reason for the vacation (e.g., "Summer vacation", "Holiday break")';
