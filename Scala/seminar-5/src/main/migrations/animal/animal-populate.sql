--liquibase formatted sql

--changeset author:e.n.konovalov runInTransaction:true failOnError:true
insert into animal_info(id, name, habitat_id, description, domesticated_year) values
(1, 'rabbit', 0, 'jumpy boi', 600),
(2, 'shark', 4, 'swimmy boi', null);

insert into voices(id, animal_id, voice) values
      (0,1,'purrr'),
      (1,1,'thump'),
      (2,1,'grrrrrrrrrr'),
      (3,1,'oink'),
      (4,1,'nurf'),
      (5,2,'ahhh'),
      (6,2,'ehh'),
      (7,2,'arara');

insert into features(id, feature) values
(0, 'long ears'),
(1, 'small tail'),
(2, 'sharp teeth'),
(3, 'fin');

insert into animal_features(feature_id, animal_id) values
(0, 1),
(1, 1),
(2, 2),
(3, 2);
--rollback delete from animal_info
