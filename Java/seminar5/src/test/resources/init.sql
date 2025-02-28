CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    name       TEXT    NOT NULL,
    balance    INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

