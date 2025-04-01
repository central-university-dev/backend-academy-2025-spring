package backend.academy.kafka.service;

import static backend.academy.kafka.model.UserEvent.UserEventType.ACCRUAL;
import backend.academy.kafka.model.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventsService {

    private final AtomicLong idGenerator = new AtomicLong();
    private final KafkaTemplate<Long, String> template;
    private final ObjectMapper objectMapper;

    @Value("${app.user-events.topic}")
    private String topic;

    public void sendMessages(long userId, int count) {
        log.info("Отправляем {} сообщений по клиенту с ИД {}", count, userId);
        if (template.isTransactional()) {
            sendMessagesV2(userId, count);
        } else {
            sendMessagesV1(userId, count);
        }
    }

    public void sendMessagesV1(long userId, int count) {
        for (int i = 0; i < count; i++) {
            template.send(topic, userId, createRandomEvent(userId));
        }
    }

    public void sendMessagesV2(long userId, int count) {
        template.executeInTransaction(ops -> {
            for (int i = 0; i < count; i++) {
                ops.send(topic, userId, createRandomEvent(userId));
            }
            return true;
        });
    }

    @SneakyThrows
    private String createRandomEvent(long userId) {
        return objectMapper.writeValueAsString(
            new UserEvent()
                .setUserId(userId)
                .setType(ACCRUAL)
                .setCreatedAt(LocalDateTime.now())
                .setId(idGenerator.incrementAndGet()));
    }

}
