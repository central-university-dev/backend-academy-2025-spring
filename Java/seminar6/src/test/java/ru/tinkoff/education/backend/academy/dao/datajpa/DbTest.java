package ru.tinkoff.education.backend.academy.dao.datajpa;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@ExtendWith(SpringExtension.class)
@EnableJpaRepositories(basePackages = "ru.tinkoff.education.backend.academy.dao.datajpa.repo")
@EntityScan("ru.tinkoff.education.backend.academy.model.entity")
@Sql("classpath:scripts/init.sql")
@ActiveProfiles("spring-data-jpa")
public class DbTest {
    @Container
    @ServiceConnection
    private static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16");
}
