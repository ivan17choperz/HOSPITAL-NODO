CREATE TABLE doctors (
    id             UUID         PRIMARY KEY,
    first_name     VARCHAR(255) NOT NULL,
    last_name      VARCHAR(255) NOT NULL,
    specialty      VARCHAR(255) NOT NULL,
    license_number VARCHAR(100) UNIQUE,
    email          VARCHAR(255),
    node_id        VARCHAR(50)  NOT NULL,
    created_at     TIMESTAMP    NOT NULL
);
