-- V3: Fix enum constraints to match Java uppercase enums
-- Date: 2024-11-26
-- Description: Update check constraints to accept uppercase enum values

-- Drop and recreate users role constraint
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check
    CHECK (role IN ('BUSINESS', 'CUSTOMER', 'ADMIN'));

-- Drop and recreate businesses category constraint
ALTER TABLE businesses DROP CONSTRAINT IF EXISTS businesses_category_check;
ALTER TABLE businesses ADD CONSTRAINT businesses_category_check
    CHECK (category IN ('HAIRDRESSER', 'BEAUTY', 'HEALTH', 'SPORT', 'GARAGE', 'OTHER'));

-- Drop and recreate appointments status constraint
ALTER TABLE appointments DROP CONSTRAINT IF EXISTS appointments_status_check;
ALTER TABLE appointments ADD CONSTRAINT appointments_status_check
    CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW'));

-- Drop and recreate notifications type constraint
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;
ALTER TABLE notifications ADD CONSTRAINT notifications_type_check
    CHECK (type IN ('CONFIRMATION', 'REMINDER', 'CANCELLATION', 'MODIFICATION'));

-- Drop and recreate notifications channel constraint
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_channel_check;
ALTER TABLE notifications ADD CONSTRAINT notifications_channel_check
    CHECK (channel IN ('EMAIL', 'SMS'));

-- Drop and recreate notifications status constraint
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_status_check;
ALTER TABLE notifications ADD CONSTRAINT notifications_status_check
    CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'DELIVERED'));

-- Drop and recreate subscriptions plan constraint
ALTER TABLE subscriptions DROP CONSTRAINT IF EXISTS subscriptions_plan_check;
ALTER TABLE subscriptions ADD CONSTRAINT subscriptions_plan_check
    CHECK (plan IN ('FREE', 'STARTER', 'PRO'));

-- Drop and recreate subscriptions status constraint
ALTER TABLE subscriptions DROP CONSTRAINT IF EXISTS subscriptions_status_check;
ALTER TABLE subscriptions ADD CONSTRAINT subscriptions_status_check
    CHECK (status IN ('ACTIVE', 'CANCELLED', 'EXPIRED', 'SUSPENDED'));
