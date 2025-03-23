package backend.academy.kafka.config;

import backend.academy.kafka.config.properties.UserEventsTopicProperties;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class CommonKafkaConfig {

    private final UserEventsTopicProperties userEventsTopicProperties;
    private final KafkaProperties kafkaProperties;

    @Bean
    Admin localKafkaClusterAdmin() {
        return AdminClient.create(
            Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaProperties.getBootstrapServers()));
    }

    @Bean
    @SneakyThrows
    ZooKeeper localZooKeeperAdmin(CuratorFramework curatorFramework) {
        return curatorFramework.getZookeeperClient().getZooKeeper();
    }

    @Bean
    @SneakyThrows
    NewTopic userEventsTopic() {
        log.info(
            "Создаем топик {} в локальном кластере Kafka: {}",
            userEventsTopicProperties.getTopic(), userEventsTopicProperties);
        return userEventsTopicProperties.toNewTopic();
    }

}
