-- +goose Up
-- +goose StatementBegin
begin;

create table if not exists example (
    id bigint primary key generated always as identity,
    status text not null default 'new',
    created_at timestamptz not null default now()
);

insert into example (status, created_at)
values
    ('disabled', now()-'5day'::interval),
    ('handled', now()-'4day'::interval),
    ('done', now()-'3day'::interval),
    ('deleted', now()-'2day'::interval),
    ('completed', now()-'1day'::interval),
    ('new', now());

end;

-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
drop table if exists example;
-- +goose StatementEnd
