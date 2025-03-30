-- DDL

create table public.users (
	id serial primary key,
	name varchar(255) not null,
	email varchar(255) unique not null
);


alter table users add column adress varchar(255)

drop table users


-- DML

insert into users (name, email) values ('Herman', 'alice@tbank.ru');

update users set name = 'Alice' where id = 1;

delete from users where id = 3;

-- DQL

select * from users

select * from users where name = 'Alice'

select * from users order by id desc

select name, count(*) from users group by name having count(*) > 1;

-- TCL

begin;
update users set email = 'new@gmail.com' where id = 1;
rollback;

begin;
update users set email = 'new@gmail.com' where id = 1;
commit;

-- DCL

select current_user;

create role user_read;

set role user_read;

set role postgres;

grant select on users to user_read;

revoke select on users from user_read;


