package backend.academy.seminar4.tc.auto;

import java.util.Map;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
class PostgresStaticAutoContainerTest {

    @Container
    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        "postgres:15"
    );

    @Test
    @Order(1)
    void shouldCreateTable() {
        var user = postgres.getUsername();
        var password = postgres.getPassword();
        var jdbcUrl = postgres.getJdbcUrl();

        JdbcClient jdbcClient = JdbcClient.create(
            new SingleConnectionDataSource(jdbcUrl, user, password, false)
        );

        jdbcClient.sql("CREATE TABLE test (id BIGSERIAL PRIMARY KEY, data TEXT NOT NULL)")
            .update();

        int inserted = jdbcClient.sql("INSERT INTO test (data) VALUES ('test data')")
            .update();

        assertEquals(1, inserted);
    }

    @Test
    @Order(2)
    void shouldSelectFromCreatedTable() {
        var user = postgres.getUsername();
        var password = postgres.getPassword();
        var jdbcUrl = postgres.getJdbcUrl();

        JdbcClient jdbcClient = JdbcClient.create(
            new SingleConnectionDataSource(jdbcUrl, user, password, false)
        );

        Map<String, Object> data = jdbcClient.sql("SELECT * FROM test")
            .query()
            .singleRow();

        assertEquals(1L, data.get("id"));
        assertEquals("test data", data.get("data"));
    }
}
