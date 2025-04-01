package backend.academy.kafka.consumer;

import backend.academy.kafka.model.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import static org.springframework.kafka.retrytopic.TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventsMessageConsumer {

    @Value("${app.user-events.fail-on-processing}")
    private boolean failOnProcessing;

    @SneakyThrows
    @KafkaListener(
            containerFactory = "defaultConsumerFactory",
            topicPartitions = @TopicPartition(topic = "${app.user-events.topic}", partitions = { "0" })
    )
    @RetryableTopic(
            backoff = @Backoff(delay = 3000L, multiplier = 2.0),
            attempts = "2", autoCreateTopics = "false",
            kafkaTemplate = "userEventKafkaTemplate",
            topicSuffixingStrategy = SUFFIX_WITH_INDEX_VALUE,
            include = RuntimeException.class
    )
    public void consume(ConsumerRecord<Long, UserEvent> record, Acknowledgment acknowledgment) {
        log.info(
                """
                Получено следующее сообщение из топика {}:
                key: {},
                value: {}
                """,
                record.topic(), record.key(), record.value());

        if (failOnProcessing && record.value().getId() % 2 == 0) {
            log.error("Эмулируем ошибку обработки данных!");
            throw new RuntimeException("Ошибка обработки данных!");
        }

        acknowledgment.acknowledge();
    }

}
