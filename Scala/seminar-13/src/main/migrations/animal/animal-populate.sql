--liquibase formatted sql

--changeset author:e.n.konovalov runInTransaction:true failOnError:true
insert into habitat(id, val) values
(0, 'forest'),
(1, 'plains'),
(2, 'desert'),
(3, 'mountains'),
(4, 'ocean');

insert into animal_info(id, name, habitat_id, description, domesticated_year) values
(1, 'rabbit', 0, 'jumpy boi', 600),
(2, 'shark', 4, 'swimmy boi', null);

insert into voices(animal_id, voice) values
      (1,'purrr'),
      (1,'thump'),
      (1,'grrrrrrrrrr'),
      (1,'oink'),
      (1,'nurf'),
      (2,'ahhh'),
      (2,'ehh'),
      (2,'arara');

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
