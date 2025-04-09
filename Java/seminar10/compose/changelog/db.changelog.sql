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

--changeset s.a.khvatov:2-init-data
insert into accounts (account_id, account_number) values (1, 'account_1'), (2, 'account_2');
insert into users (user_id, account_id) values (30, 1), (32, 2);
insert into account_scores (account_id, score) values (1, 100);
