-- Revert gender column back to VARCHAR from enum type
-- This fixes the JPA type mismatch error

-- First, convert the column back to VARCHAR
ALTER TABLE doctors ALTER COLUMN gender TYPE VARCHAR(10);

-- Drop the enum type
DROP TYPE IF EXISTS gender_enum;
