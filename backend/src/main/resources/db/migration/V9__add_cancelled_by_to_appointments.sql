-- Add cancelled_by column to track who cancelled the appointment (if not exists)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'appointments' AND column_name = 'cancelled_by') THEN
        ALTER TABLE appointments ADD COLUMN cancelled_by VARCHAR(20);
    END IF;
END $$;

-- Drop existing constraint if it exists (in case V8 was partially applied)
ALTER TABLE appointments DROP CONSTRAINT IF EXISTS chk_cancelled_by;

-- Add check constraint for cancelled_by values
ALTER TABLE appointments ADD CONSTRAINT chk_cancelled_by
    CHECK (cancelled_by IS NULL OR cancelled_by IN ('CUSTOMER', 'BUSINESS'));

-- Add comment
COMMENT ON COLUMN appointments.cancelled_by IS 'Indicates who cancelled the appointment: CUSTOMER or BUSINESS';
