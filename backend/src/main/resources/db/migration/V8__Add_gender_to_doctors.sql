-- Add gender column to doctors table
ALTER TABLE doctors ADD COLUMN gender VARCHAR(10);

-- Set default gender for existing doctors (optional, can be left NULL)
-- UPDATE doctors SET gender = 'male' WHERE gender IS NULL;
