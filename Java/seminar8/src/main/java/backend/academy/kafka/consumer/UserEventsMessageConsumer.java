package backend.academy.kafka.consumer;

import static org.springframework.kafka.retrytopic.TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE;
import backend.academy.kafka.model.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventsMessageConsumer {

    @SneakyThrows
    @KafkaListener(
        containerFactory = "defaultConsumerFactory",
        topicPartitions = @TopicPartition(topic = "${app.user-events.topic}", partitions = { "0" })
    )
    @RetryableTopic(
        backoff = @Backoff(delay = 3000L, multiplier = 2.0),
        attempts = "2", autoCreateTopics = "false",
        kafkaTemplate = "userEventKafkaTemplate",
        topicSuffixingStrategy = SUFFIX_WITH_INDEX_VALUE
    )
    public void consume(ConsumerRecord<Long, UserEvent> record, Acknowledgment acknowledgment) {
        log.info(
            """
            Получено следующее сообщение из топика {}:
            key: {},
            value: {}
            """,
            record.topic(), record.key(), record.value());

        if (record.value().getId() % 2 == 0) {
            log.error("Эмулируем ошибку обработки данных!");
            throw new RuntimeException("Ошибка обработки данных!");
        }

        acknowledgment.acknowledge();
    }

}
