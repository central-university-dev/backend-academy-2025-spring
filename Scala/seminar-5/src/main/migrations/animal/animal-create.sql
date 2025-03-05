--liquibase formatted sql

--changeset author:e.n.konovalov runInTransaction:true failOnError:true
create table animal_info ( -- question: why we use `if not exists`?
    id int auto_increment primary key,
    name varchar not null,
    description varchar not null,
    habitat varchar not null, -- task1 for students: how fix it? (enum)
    domesticated_year int null
);

-- scala model
--    description: String,
--    habitat: Habitat,
--    features: List[String],
--    domesticatedYear: Option[Int],
--    voice: Option[Vector[String]],

-- task2 for students: add full support of model
-- task3 for students: database normalization

--rollback drop table animal_info