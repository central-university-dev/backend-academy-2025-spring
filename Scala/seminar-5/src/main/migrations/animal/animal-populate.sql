--liquibase formatted sql

--changeset author:e.n.konovalov runInTransaction:true failOnError:true
insert into animal_info(id, name, habitat, description, domesticated_year) values
(1, 'rabbit', 'forest', 'jumpy boi', 600),
(2, 'shark', 'ocean', 'swimmy boi', null);
--rollback delete from animal_info
