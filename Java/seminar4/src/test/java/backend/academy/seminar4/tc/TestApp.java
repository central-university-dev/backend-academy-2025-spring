package backend.academy.seminar4.tc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class TestApp {

    public static void main(String[] args) {
        SpringApplication.from(App::main)
            .with(ContainersConfiguration.class)
            .run(args);
    }

    @TestConfiguration
    @Slf4j
    static class ContainersConfiguration {

        @Bean
        @RestartScope
        @ServiceConnection
        public PostgreSQLContainer<?> postgreSQLContainer() {
            return new PostgreSQLContainer<>("postgres:15");
        }

        @Bean
        @RestartScope
        public GenericContainer<?> mailpitContainer() {
            return new GenericContainer("axllent/mailpit:v1.22.3")
                .withExposedPorts(1025, 8025)
                .waitingFor(Wait.forLogMessage(".*accessible via.*", 1));
        }

        @Bean
        public DynamicPropertyRegistrar mailpitProperties(GenericContainer<?> mailpitContainer) {
            return properties -> {
                log.info(
                    "Mailpit SMTP port : {}, WEB port: {}",
                    mailpitContainer.getMappedPort(1025),
                    mailpitContainer.getMappedPort(8025)
                );
                properties.add("spring.mail.host", mailpitContainer::getHost);
                properties.add("spring.mail.port", mailpitContainer::getFirstMappedPort);
            };
        }
    }
}
