package backend.academy.kafka.service;

import static backend.academy.kafka.config.KafkaProducerConfig.AVRO_KAFKA_TEMPLATE_BEAN;
import static backend.academy.kafka.config.KafkaProducerConfig.GENERIC_KAFKA_TEMPLATE_BEAN;
import backend.academy.kafka.config.properties.UserEventsTopicProperties;
import backend.academy.kafka.model.CustomUserEvent;
import backend.academy.kafka.model.CustomUserEvent.CustomUserEventType;
import backend.academy.kafka.model.generated.UserEvent;
import backend.academy.kafka.model.generated.UserEventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({ "rawtypes", "unchecked" })
public class UserEventsService {

    private final AtomicLong idGenerator = new AtomicLong();

    private final UserEventsTopicProperties topicProperties;
    private final ObjectMapper objectMapper;

    @Qualifier(GENERIC_KAFKA_TEMPLATE_BEAN)
    private final KafkaTemplate genericTemplate;

    @Qualifier(AVRO_KAFKA_TEMPLATE_BEAN)
    private final KafkaTemplate avroTemplate;

    public void sendMessages(long userId, int count) {
        sendMessages(userId, count, false, false);
    }

    public void sendMessages(long userId, int count, boolean useAvro, boolean inTransaction) {
        log.info("Отправляем {} сообщений по клиенту с ИД {}", count, userId);
        final var template = useAvro ? avroTemplate : genericTemplate;
        if (inTransaction && !template.isTransactional()) {
            throw new UnsupportedOperationException("Используемый KafkaTemplate не поддерживает транзакции");
        }

        final var topic = useAvro ? topicProperties.getAvroTopic() : topicProperties.getTopic();
        final Supplier<Object> eventSupplier = () -> useAvro ? createRawRandomEvent(userId) : createRandomEvent(userId);

        if (inTransaction) {
            sendMessagesV2(template, topic, userId, count, eventSupplier);
        } else {
            sendMessagesV1(template, topic, userId, count, eventSupplier);
        }
    }

    @SneakyThrows
    private void sendMessagesV1(
        KafkaTemplate template, String topic,
        long userId, int count,
        Supplier<Object> eventSupplier
    ) {
        for (int i = 0; i < count; i++) {
            template.send(topic, userId, eventSupplier.get());
        }
    }

    private void sendMessagesV2(
        KafkaTemplate template, String topic,
        long userId, int count,
        Supplier<Object> eventSupplier
    ) {
        template.executeInTransaction(ops -> {
            for (int i = 0; i < count; i++) {
                ops.send(topic, userId, eventSupplier.get());
            }
            return true;
        });
    }

    @SneakyThrows
    private String createRandomEvent(long userId) {
        final var id = idGenerator.incrementAndGet();
        return objectMapper.writeValueAsString(
            new CustomUserEvent()
                .setUserId(userId)
                .setType(id % 2 == 0 ? CustomUserEventType.ACCRUAL : CustomUserEventType.WITHDRAWAL)
                .setCreatedAt(LocalDateTime.now())
                .setId(id)
        );
    }

    @SneakyThrows
    private UserEvent createRawRandomEvent(long userId) {
        final var id = idGenerator.incrementAndGet();
        return UserEvent.newBuilder()
            .setUserId(userId)
            .setEventType(id % 2 == 0 ? UserEventType.ACCRUAL : UserEventType.WITHDRAWAL)
            .setCreatedAt(LocalDateTime.now().toInstant(ZoneOffset.UTC))
            .setId(id)
            .build();
    }

}
