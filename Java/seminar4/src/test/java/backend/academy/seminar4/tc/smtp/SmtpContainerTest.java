package backend.academy.seminar4.tc.smtp;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = MailSenderAutoConfiguration.class)
@Testcontainers
class SmtpContainerTest {

    @Container
    private static final GenericContainer<?> mailpit = new GenericContainer(
        "axllent/mailpit:v1.22.3"
    )
        .withExposedPorts(1025, 8025)
        .waitingFor(Wait.forLogMessage(".*accessible via.*", 1));

    @DynamicPropertySource
    static void smtpProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", mailpit::getHost);
        registry.add("spring.mail.port", mailpit::getFirstMappedPort);
    }

    @Autowired
    private JavaMailSender mailSender;

    @Test
    void shouldSendSmtpMessage() throws Exception {
        var message = new SimpleMailMessage();
        message.setFrom("my@email.com");
        message.setTo("another@email.com");
        message.setSubject("Test");
        message.setText("Hello in email message");
        mailSender.send(message);

        Thread.sleep(Duration.ofSeconds(2));

        var restClient = RestClient.create(
            "http://" + mailpit.getHost()
                + ":" + mailpit.getMappedPort(8025)
                + "/api/v1"
        );

        var receivedMessage = restClient.get()
            .uri("/messages")
            .retrieve()
            .body(ObjectNode.class)
            .get("messages")
            .get(0);

        assertEquals(message.getText(), receivedMessage.get("Snippet").asText());
        assertEquals(message.getSubject(), receivedMessage.get("Subject").asText());
    }
}
