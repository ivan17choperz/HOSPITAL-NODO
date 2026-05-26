DROP TABLE IF EXISTS admissions;

CREATE TABLE admissions (
    id             UUID         PRIMARY KEY,
    patient_id     UUID         NOT NULL,
    first_name     VARCHAR(255) NOT NULL,
    last_name      VARCHAR(255) NOT NULL,
    gender         VARCHAR(50),
    birth_date     DATE,
    identifier     VARCHAR(100),
    ward           VARCHAR(100),
    episode_number VARCHAR(100) NOT NULL UNIQUE,
    node_id        VARCHAR(50)  NOT NULL,
    admitted_at    TIMESTAMP    NOT NULL,
    status         VARCHAR(50)  NOT NULL
);
