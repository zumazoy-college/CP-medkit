-- Add effective_to column to schedules table
ALTER TABLE schedules ADD COLUMN effective_to DATE;

-- Drop the unique constraint on (doctor_id, day_of_week) to allow multiple schedules for the same day with different date ranges
ALTER TABLE schedules DROP CONSTRAINT IF EXISTS schedules_doctor_id_day_of_week_key;

-- Add index for efficient date range queries
CREATE INDEX idx_schedules_doctor_date_range ON schedules(doctor_id, effective_from, effective_to);
CREATE INDEX idx_schedules_active ON schedules(is_active);
