CREATE TABLE IF NOT EXISTS emr_records (
    id UUID PRIMARY KEY,
    patient_id UUID,
    episode_number VARCHAR(100),
    condition_fhir_json TEXT,
    medication_request_fhir_json TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

ALTER TABLE emr_records ADD COLUMN IF NOT EXISTS admission_id UUID;
ALTER TABLE emr_records ADD COLUMN IF NOT EXISTS anamnesis TEXT;
ALTER TABLE emr_records ADD COLUMN IF NOT EXISTS clinician_notes TEXT;
ALTER TABLE emr_records ADD COLUMN IF NOT EXISTS diagnoses_json TEXT;
ALTER TABLE emr_records ADD COLUMN IF NOT EXISTS medications_json TEXT;
