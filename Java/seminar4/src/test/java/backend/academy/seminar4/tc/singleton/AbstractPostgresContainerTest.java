package backend.academy.seminar4.tc.singleton;

import org.testcontainers.containers.PostgreSQLContainer;

class AbstractPostgresContainerTest {

    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        "postgres:15"
    );

    static {
        postgres.start();
    }

}
