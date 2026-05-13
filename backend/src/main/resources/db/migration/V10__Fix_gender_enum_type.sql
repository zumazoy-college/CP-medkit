-- Create gender enum type if not exists
DO $$ BEGIN
    CREATE TYPE gender_enum AS ENUM ('male', 'female');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Convert existing gender column to use enum type
ALTER TABLE doctors ALTER COLUMN gender TYPE gender_enum USING gender::gender_enum;
