CREATE TABLE admissions (
    id          VARCHAR(36)  PRIMARY KEY,
    patient_id  VARCHAR(100) NOT NULL,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    gender      VARCHAR(20),
    birth_date  DATE,
    identifier  VARCHAR(100),
    ward        VARCHAR(100),
    admitted_at TIMESTAMP    NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ADMITTED'
);
