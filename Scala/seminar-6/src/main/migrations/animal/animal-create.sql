--liquibase formatted sql


--changeset author:e.n.konovalov runInTransaction:true failOnError:true

create table if not exists habitat (
    id int primary key,
    val varchar not null
);

create table if not exists animal_info (
    id int auto_increment primary key,
    name varchar not null,
    description varchar not null,
    habitat_id int not null,
    domesticated_year int null,
    constraint FK_habitat foreign key (habitat_id) REFERENCES habitat (id)
);

create table if not exists voices (
    id int auto_increment primary key,
    animal_id int not null,
    voice varchar not null,
    constraint FK_voices_animal_info foreign key (animal_id) REFERENCES animal_info (id)
);

create table if not exists features (
    id int auto_increment primary key,
    feature varchar not null
);

create table if not exists animal_features (
    feature_id int not null,
    animal_id int not null,
    constraint PK_animal_features primary key (feature_id, animal_id),
    constraint FK_animal_features_animal foreign key (animal_id) REFERENCES animal_info (id),
    constraint FK_animal_features_feature foreign key (feature_id) REFERENCES features (id)
);

--rollback drop table animal_info