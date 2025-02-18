package backend.academy.seminar4.tc.auto;

import java.sql.DriverManager;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
// OR: @ExtendWith(TestcontainersExtension.class)
class PostgresAutoContainerTest {

    @Container
    private PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        "postgres:15"
    );

    @Test
    void shouldExecuteQuery() {
        var user = postgres.getUsername();
        var password = postgres.getPassword();
        var jdbcUrl = postgres.getJdbcUrl();

        try (
            var connection = DriverManager.getConnection(jdbcUrl, user, password);
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SELECT 1")
        ) {
            assertTrue(resultSet.next());
            int result = resultSet.getInt(1);
            assertEquals(1, result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
