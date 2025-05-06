insert into hotels (name, address)
values ('Grand Maple Inn', '123 Oak St, Springfield'),
       ('Seaside Escape', '456 Beach Rd, Oceanside'),
       ('Mountain View Lodge', '789 Hilltop Dr, Boulder'),
       ('Urban Nest Hotel', '321 Main St, Metropolis'),
       ('Forest Retreat', '654 Pine Ln, Timberland')
    on conflict do nothing;

insert into rooms (room_type_id, hotel_id, name)
values (1, 1, 'Room 101'),
       (2, 1, 'Room 102'),
       (1, 1, 'Room 103'),
       (2, 1, 'Room 104'),
       (1, 1, 'Room 105'),

       (3, 2, 'Room 201'),
       (4, 2, 'Room 202'),
       (3, 2, 'Room 203'),
       (4, 2, 'Room 204'),
       (3, 2, 'Room 205'),

       (5, 3, 'Room 301'),
       (6, 3, 'Room 302'),
       (5, 3, 'Room 303'),
       (6, 3, 'Room 304'),
       (5, 3, 'Room 305'),

       (7, 4, 'Room 401'),
       (8, 4, 'Room 402'),
       (7, 4, 'Room 403'),
       (8, 4, 'Room 404'),
       (7, 4, 'Room 405'),

       (9, 5, 'Room 501'),
       (10, 5, 'Room 502'),
       (9, 5, 'Room 503'),
       (10, 5, 'Room 504'),
       (9, 5, 'Room 505')
on conflict do nothing;
