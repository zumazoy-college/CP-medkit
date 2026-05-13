-- Add gender column to users table
ALTER TABLE users ADD COLUMN gender VARCHAR(10);

-- Migrate data from doctors to users
UPDATE users u
SET gender = d.gender::VARCHAR
FROM doctors d
WHERE u.id_user = d.user_id AND d.gender IS NOT NULL;

-- Migrate data from patients to users (only if not already set from doctors)
UPDATE users u
SET gender = p.gender::VARCHAR
FROM patients p
WHERE u.id_user = p.user_id AND u.gender IS NULL AND p.gender IS NOT NULL;

-- Remove gender column from doctors
ALTER TABLE doctors DROP COLUMN IF EXISTS gender;

-- Remove gender column from patients
ALTER TABLE patients DROP COLUMN gender;
