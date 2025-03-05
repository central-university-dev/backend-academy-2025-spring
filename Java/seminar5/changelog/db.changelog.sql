--liquibase formatted sql

--changeset rushan:1-init-schema
CREATE TABLE users(
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    balance INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

--changeset rushan:2-populate-db
INSERT INTO users(name, balance) VALUES ('Alice', 1000);
INSERT INTO users(name, balance) VALUES ('Antony', 1200);
INSERT INTO users(name, balance) VALUES ('Andrew', 4567);
INSERT INTO users(name, balance) VALUES ('Bob', 1001);
INSERT INTO users(name, balance) VALUES ('Kate', 1021);
