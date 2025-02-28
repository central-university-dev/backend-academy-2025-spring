package backend.academy.seminar4.tc.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

@SpringBootTest(classes = KafkaAutoConfiguration.class)
@Testcontainers
// close consumers and producers after test
@DirtiesContext(classMode = AFTER_CLASS)
class KafkaContainerTest {

    @ServiceConnection
    @Container
    private static final KafkaContainer kafka = new KafkaContainer(
        "apache/kafka-native:3.8.1"
    );

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Test
    void shouldSendKafkaMessage() throws Exception {
        kafkaTemplate.setConsumerFactory(consumerFactory);

        var sendResult = kafkaTemplate.send("test-topic", "Test message")
            .get();

        var partition = sendResult.getRecordMetadata().partition();
        var offset = sendResult.getRecordMetadata().offset();

        var receivedMessage = kafkaTemplate.receive("test-topic", partition, offset)
            .value();

        assertEquals("Test message", receivedMessage);
    }
}
