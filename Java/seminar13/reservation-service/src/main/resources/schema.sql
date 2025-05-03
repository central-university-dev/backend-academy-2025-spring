create table if not exists reservations
(
    id           bigserial primary key,
    hotel_id     bigint not null,
    room_type_id bigint not null,
    start_date   date   not null,
    end_date     date   not null,
    guest_id     bigint not null
);

create table if not exists room_type_inventory
(
    hotel_id        bigint not null,
    room_type_id    bigint not null,
    date            date   not null default current_date,
    total_inventory int    not null default 0,
    total_reserved  int    not null default 0,
    primary key (hotel_id, room_type_id, date)
);
