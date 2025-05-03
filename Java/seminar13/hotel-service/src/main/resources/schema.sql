create table if not exists hotels
(
    id      bigserial primary key,
    name    text not null unique,
    address text not null
);

create table if not exists rooms
(
    id           bigserial primary key,
    room_type_id bigint  not null,
    hotel_id     bigint  not null references hotels (id),
    name         text    not null,
    unique (hotel_id, name)
);

