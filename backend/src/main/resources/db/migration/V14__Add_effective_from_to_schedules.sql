ALTER TABLE schedules ADD COLUMN effective_from DATE;

-- Set default to current date for existing schedules
UPDATE schedules SET effective_from = CURRENT_DATE WHERE effective_from IS NULL;
