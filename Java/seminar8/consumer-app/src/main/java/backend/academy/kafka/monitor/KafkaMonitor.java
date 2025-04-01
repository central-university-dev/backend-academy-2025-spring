package backend.academy.kafka.monitor;

import backend.academy.kafka.config.properties.UserEventsTopicProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("rawtypes")
public class KafkaMonitor {

    private final List<AbstractMessageListenerContainer> containers;
    private final UserEventsTopicProperties topicProperties;
    private final Admin admin;

    @SneakyThrows
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void logKafkaClusterState() {
        containers.forEach(this::describeContainer);
    }

    @SneakyThrows
    private void describeContainer(AbstractMessageListenerContainer container) {
        var groupId = container.getGroupId();
        if (groupId == null) {
            return;
        }

        var groupDescription = admin.describeConsumerGroups(List.of(groupId))
                .describedGroups().get(groupId).get();
        log.info(
                """
                В системе зарегистрирована группа консьюмеров {}:
                - Координатор: {} ({}/{}),
                - Используемый assignor: {}
                - Состояние: {}
                - Консьюмеры: \n\t- {}
                """,
                groupDescription.groupId(),
                groupDescription.coordinator().id(),
                groupDescription.coordinator().host(),
                groupDescription.coordinator().port(),
                groupDescription.partitionAssignor(),
                groupDescription.state().name(),
                groupDescription.members().stream()
                        .map(it -> "ИД: %s, хост: %s".formatted(it.consumerId(), it.host()))
                        .collect(Collectors.joining("\n\t- ")));
    }

}
