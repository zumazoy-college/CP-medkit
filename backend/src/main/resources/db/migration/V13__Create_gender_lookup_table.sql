-- Create genders lookup table
CREATE TABLE genders (
    id_gender INTEGER PRIMARY KEY,
    name VARCHAR(10) NOT NULL UNIQUE
);

-- Insert gender values with fixed IDs
INSERT INTO genders (id_gender, name) VALUES (1, 'male'), (2, 'female');

-- Add gender_id column to users table
ALTER TABLE users ADD COLUMN gender_id INTEGER;

-- Migrate existing data from gender string to gender_id
UPDATE users
SET gender_id = CASE
    WHEN LOWER(gender) = 'male' THEN 1
    WHEN LOWER(gender) = 'female' THEN 2
    ELSE NULL
END;

-- Add foreign key constraint
ALTER TABLE users ADD CONSTRAINT fk_users_gender
    FOREIGN KEY (gender_id) REFERENCES genders(id_gender);

-- Drop old gender column
ALTER TABLE users DROP COLUMN gender;
