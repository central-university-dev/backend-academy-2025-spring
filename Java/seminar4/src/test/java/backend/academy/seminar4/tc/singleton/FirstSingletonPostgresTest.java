package backend.academy.seminar4.tc.singleton;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

class FirstSingletonPostgresTest extends AbstractPostgresContainerTest {

    @Test
    void shouldExecuteQuery() {
        JdbcClient.create(
                new SingleConnectionDataSource(
                    postgres.getJdbcUrl(),
                    postgres.getUsername(),
                    postgres.getPassword(),
                    false
                )
            ).sql("SELECT 1")
            .query()
            .singleValue();
    }
}
