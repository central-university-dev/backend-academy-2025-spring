package ru.tbank.sem5.springapp;

import jakarta.annotation.PostConstruct;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@JdbcTest
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("seminar5")
        .withUsername("test")
        .withPassword("test")
        .withInitScript("init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    private UserRepository userRepository;

    @PostConstruct // создаём UserRepository только после внедрения зависимостей
    void setUp() {
        userRepository = new UserRepository(jdbcTemplate, namedJdbcTemplate);
    }

    @Test
    void getUsers() {
        jdbcTemplate.update("INSERT INTO users (name, balance) VALUES ('Bob', 2000)");

        var users = userRepository.getUsers();

        Assertions.assertThat(users)
            .containsExactly(new User("Bob", 2000));
    }

    // написать тесты на семинаре
}
