--liquibase formatted sql

--changeset s.a.khvatov:1-init-schema
create table accounts
(
    account_id     bigserial primary key,
    account_number varchar(32) not null unique
);

create table account_scores
(
    account_id bigserial primary key,
    score      integer not null,
    updated_at timestamp default now(),

    constraint fk_accounts
        foreign key (account_id) references accounts
);

create table users
(
    user_id    bigserial primary key,
    account_id integer not null,

    constraint fk_accounts
        foreign key (account_id) references accounts
);
