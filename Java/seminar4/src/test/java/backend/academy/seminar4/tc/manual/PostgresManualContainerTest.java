package backend.academy.seminar4.tc.manual;

import java.sql.DriverManager;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class PostgresManualContainerTest {

    @Test
    void shouldExecuteQueryWithGenericContainer() {
        var user = "test";
        var password = "test";

        GenericContainer<?> postgres = new GenericContainer<>("postgres:15")
            .withExposedPorts(5432) // exposed port INSIDE the container
            .withEnv("POSTGRES_USER", user)
            .withEnv("POSTGRES_PASSWORD", password)
            .waitingFor(
                Wait.forLogMessage(
                    ".*database system is ready to accept connections.*",
                    2
                )
            )
            .withStartupTimeout(Duration.ofSeconds(30));

        postgres.start();

        var host = postgres.getHost();
        // mapped port of the container on HOST system
        var port = postgres.getMappedPort(5432);
        var jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/test";

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

    @Test
    void shouldExecuteQueryWithPostgresContainer() {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test"); // optional, will set the default

        // Log:
        // Container is started (JDBC URL: jdbc:postgresql://localhost:32779/test?loggerLevel=OFF)
        postgres.start();

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

        postgres.stop();
    }
}
