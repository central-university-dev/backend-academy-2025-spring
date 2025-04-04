package backend.academy.kafka.consumer;

import backend.academy.kafka.model.generated.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventsAvroMessageConsumer {

    @SneakyThrows
    @KafkaListener(
        containerFactory = "avroConsumerFactory",
        topics = "${app.user-events.avro-topic}"
    )
    public void consume(ConsumerRecord<Long, UserEvent> record, Acknowledgment acknowledgment) {
        final var value = record.value();
        log.info(
            """
            Получено следующее сообщение из avro-топика {}:
            key: {},
            avro-value: {},
            значение типа UserEvent: {}
            """,
            record.topic(), record.key(), value.toString(), value);

        acknowledgment.acknowledge();
    }

}
