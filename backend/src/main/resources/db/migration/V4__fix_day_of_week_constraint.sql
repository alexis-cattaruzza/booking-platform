-- V4: Fix day_of_week constraint to match Java DayOfWeek enum
-- Date: 2025-11-26
-- Description: Update schedules day_of_week constraint to accept full uppercase day names

-- Drop and recreate schedules day_of_week constraint
ALTER TABLE schedules DROP CONSTRAINT IF EXISTS schedules_day_of_week_check;
ALTER TABLE schedules ADD CONSTRAINT schedules_day_of_week_check
    CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'));
