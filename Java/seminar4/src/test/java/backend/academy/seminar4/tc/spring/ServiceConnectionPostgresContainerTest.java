package backend.academy.seminar4.tc.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@Testcontainers
class ServiceConnectionPostgresContainerTest {

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        "postgres:15"
    );

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void shouldExecuteQuery() {
        var result = jdbcClient.sql("SELECT 1")
            .query()
            .singleValue();

        assertEquals(1, result);
    }
}
