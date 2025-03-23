package backend.academy.kafka.monitor;

import backend.academy.kafka.config.properties.UserEventsTopicProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMonitor {

    private final UserEventsTopicProperties topicProperties;
    private final Admin client;

    @SneakyThrows
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void logKafkaClusterState() {
        var clusterDescription = client.describeCluster();

        log.info(
                "Кластер состоит из следующих нод: {}",
                clusterDescription.nodes().get().stream()
                        .map(it -> "\n\t- ИД: %s, хост: %s:%s".formatted(it.id(), it.host(), it.port()))
                        .collect(Collectors.joining(", ")));

        log.info("ИД контроллера: {}", clusterDescription.controller().get().id());

        var topicsDescription = client.describeTopics(List.of(topicProperties.getTopic()));
        var topicDescription = topicsDescription.allTopicNames().get().get(topicProperties.getTopic());

        log.info(
                "Топик: {}, партиции: {}", topicDescription.name(),
                topicDescription.partitions().stream()
                        .map(it -> "\n\t- ИД: %s, лидер: %s, \n\tреплики:\n\t\t%s \n\tISR: \n\t\t%s"
                                .formatted(
                                        it.partition(), it.leader().id(),
                                        it.replicas().stream()
                                                .map(String::valueOf)
                                                .collect(Collectors.joining("\n\t\t")),
                                        it.isr().stream()
                                                .map(String::valueOf)
                                                .collect(Collectors.joining("\n\t\t"))))
                        .collect(Collectors.joining(", ")));

        var groupDescription = client.describeConsumerGroups(List.of(topicProperties.getAppGroupId()))
                .all().get().get(topicProperties.getAppGroupId());
        log.info(
                """
                В системе зарегистрирована группа консьюмеров {}:
                - Координатор: {} ({}/{}),
                - Используемый assignor: {}
                - Консьюмеры: \n\t- {}
                """,
                groupDescription.groupId(),
                groupDescription.coordinator().id(),
                groupDescription.coordinator().host(),
                groupDescription.coordinator().port(),
                groupDescription.partitionAssignor(),
                groupDescription.members().stream()
                        .map(it -> "ИД: %s, хост: %s".formatted(it.consumerId(), it.host()))
                        .collect(Collectors.joining("\n\t- ")));
    }

}
