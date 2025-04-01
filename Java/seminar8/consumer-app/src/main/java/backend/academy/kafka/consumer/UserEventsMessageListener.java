package backend.academy.kafka.consumer;

import backend.academy.kafka.model.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.BatchAcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventsMessageListener implements BatchAcknowledgingMessageListener<String, byte[]> {

    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(List<ConsumerRecord<String, byte[]>> data, Acknowledgment acknowledgment) {
        log.info(
                "Получили пачку сообщений размером {} штук! Вычитанные партиции: {}.",
                data.size(),
                data.stream()
                        .map(ConsumerRecord::partition)
                        .collect(Collectors.toSet()));
        data.forEach(this::onMessage);
        Objects.requireNonNull(acknowledgment).acknowledge();
    }

    @SneakyThrows
    private void onMessage(ConsumerRecord<String, byte[]> data) {
        var event = objectMapper.readValue(data.value(), UserEvent.class);
        log.info(
                "Обрабатываем событие с ИД {} по клиенту с ИД {}. Топик: {}, партиция: {}, смещение: {}.",
                event.getId(), event.getUserId(), data.topic(), data.partition(), data.offset());
    }

}
