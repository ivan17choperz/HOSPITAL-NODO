ALTER TABLE emr_records ADD COLUMN IF NOT EXISTS admission_id UUID;
ALTER TABLE emr_records ADD COLUMN IF NOT EXISTS anamnesis TEXT;
ALTER TABLE emr_records ADD COLUMN IF NOT EXISTS clinician_notes TEXT;
ALTER TABLE emr_records ADD COLUMN IF NOT EXISTS diagnoses_json TEXT;
ALTER TABLE emr_records ADD COLUMN IF NOT EXISTS medications_json TEXT;
