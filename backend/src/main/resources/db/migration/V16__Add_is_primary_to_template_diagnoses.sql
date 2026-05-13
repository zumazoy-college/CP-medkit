-- Add is_primary column to template_diagnoses table
ALTER TABLE template_diagnoses
ADD COLUMN is_primary BOOLEAN NOT NULL DEFAULT FALSE;

-- Add comment to the column
COMMENT ON COLUMN template_diagnoses.is_primary IS 'Indicates if this is the primary diagnosis in the template';
