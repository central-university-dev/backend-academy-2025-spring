package backend.academy.kafka.controller;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
public class UserEventsController {

    private final AtomicLong idGenerator = new AtomicLong();
    private final KafkaTemplate<Long, String> template;
    private final ObjectMapper objectMapper;

    @Value("${app.user-events.topic}")
    private String topic;

    @PostMapping("/users/{user-id}/events")
    public void sendMessages(
        @PathVariable("user-id") long userId,
        @RequestParam(name = "count", defaultValue = "30") int count,
        @RequestParam(name = "force", defaultValue = "false") boolean force,
        @RequestParam(name = "transactional", defaultValue = "false") boolean transactional
    ) {
        log.info("Отправляем {} сообщений по клиенту с ИД {}", count, userId);

        if (transactional && template.isTransactional()) {
            template.executeInTransaction(ops -> {
                for (int i = 0; i < count; i++) {
                    ops.send(topic, userId, createRandomEvent(userId));
                }
                return true;
            });
        } else {
            for (int i = 0; i < count; i++) {
                template.send(topic, userId, createRandomEvent(userId));
            }
            if (force) {
                template.flush();
            }
        }
    }

    @SneakyThrows
    private String createRandomEvent(long userId) {
        return objectMapper.writeValueAsString(
            UserEvent.builder()
                .userId(userId)
                .type(ACCRUAL)
                .createdAt(LocalDateTime.now())
                .id(idGenerator.incrementAndGet())
                .build());
    }

}
