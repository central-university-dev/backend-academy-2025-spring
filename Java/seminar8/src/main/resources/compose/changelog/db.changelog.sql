--liquibase formatted sql

--changeset shvatov:1-users-table
CREATE TABLE users(
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    balance INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);

--changeset shvatov:2-user-events-table
CREATE TABLE user_events(
    id BIGSERIAL PRIMARY KEY,
    user_id BIGSERIAL NOT NULL REFERENCES users (id),
    type TEXT NOT NULL,
    data JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT now()
);
