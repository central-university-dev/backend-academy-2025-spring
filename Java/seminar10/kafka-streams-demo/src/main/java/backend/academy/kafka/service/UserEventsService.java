package backend.academy.kafka.service;

import static backend.academy.kafka.config.KafkaProducerConfig.GENERIC_KAFKA_TEMPLATE_BEAN;
import static backend.academy.kafka.dto.UserEvent.UserEventType.ACCRUAL;
import static backend.academy.kafka.dto.UserEvent.UserEventType.WITHDRAWAL;
import backend.academy.kafka.config.properties.UserEventsTopicProperties;
import backend.academy.kafka.dto.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
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
    private final KafkaTemplate template;

    public void sendMessages(long userId, int count) {
        log.info("Отправляем {} сообщений по клиенту с ИД {}", count, userId);
        for (int i = 0; i < count; i++) {
            template.send(topicProperties.getInputTopic(), userId, createRandomEvent(userId));
        }
        template.flush();
    }

    @SneakyThrows
    private String createRandomEvent(long userId) {
        final var id = idGenerator.incrementAndGet();
        return objectMapper.writeValueAsString(
            new UserEvent()
                .setUserId(userId)
                .setType(id % 2 == 0 ? ACCRUAL : WITHDRAWAL)
                .setCreatedAt(LocalDateTime.now())
                .setEventId(id)
        );
    }

}
