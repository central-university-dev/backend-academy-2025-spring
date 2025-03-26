package backend.academy.kafka.consumer;

import backend.academy.kafka.model.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventsMessageListener implements AcknowledgingMessageListener<String, byte[]> {

    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public void onMessage(ConsumerRecord<String, byte[]> data, Acknowledgment acknowledgment) {
        var event = objectMapper.readValue(data.value(), UserEvent.class);
        log.info(
            "Обрабатываем событие с ИД {} по клиенту с ИД {}. Топик: {}, партиция: {}, смещение: {}.",
            event.getId(), event.getUserId(), data.topic(), data.partition(), data.offset());
        Objects.requireNonNull(acknowledgment).acknowledge();
    }

}
