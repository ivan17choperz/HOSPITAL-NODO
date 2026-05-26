CREATE TABLE emr_records (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL,
    episode_number VARCHAR(100) NOT NULL UNIQUE,
    condition_fhir_json TEXT,
    medication_request_fhir_json TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
