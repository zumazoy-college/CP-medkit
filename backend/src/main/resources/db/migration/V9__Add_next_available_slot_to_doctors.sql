-- Add next_available_slot_date and next_available_slot_time to doctors table for sorting
ALTER TABLE doctors ADD COLUMN next_available_slot_date DATE;
ALTER TABLE doctors ADD COLUMN next_available_slot_time TIME;

-- Create index for sorting performance
CREATE INDEX idx_doctors_next_slot ON doctors(next_available_slot_date, next_available_slot_time);
