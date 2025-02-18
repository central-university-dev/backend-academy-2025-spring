package backend.academy.seminar4.tc.spring;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.AFTER_METHOD;

@JdbcTest
@Import(DirtiesContextFailedTest.DatabaseInitializer.class)
@Testcontainers
class DirtiesContextFailedTest {

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        "postgres:15"
    );

    @TestConfiguration
    static class DatabaseInitializer {

        @Bean
        public ApplicationRunner initializer(JdbcClient jdbcClient) {
            return ignore -> {
                jdbcClient.sql("CREATE TABLE test (id BIGSERIAL PRIMARY KEY, data TEXT NOT NULL);")
                    .update();

                jdbcClient.sql("INSERT INTO test (data) VALUES ('test');")
                    .update();
            };
        }
    }

    @Autowired
    private JdbcClient jdbcClient;

    // One of the below test will fail because of DatabaseInitializer
    // will try to create already existing table
    // don't forget to remove @Disabled

    @Test
    @DirtiesContext(methodMode = AFTER_METHOD)
    @Disabled
    void shouldCreateTable1() {
        assertDoesNotThrow(() -> jdbcClient.sql("SELECT * FROM test").query().singleRow());
    }

    @Test
    @DirtiesContext(methodMode = AFTER_METHOD)
    void shouldCreateTable2() {
        assertDoesNotThrow(() -> jdbcClient.sql("SELECT * FROM test").query().singleRow());
    }
}
