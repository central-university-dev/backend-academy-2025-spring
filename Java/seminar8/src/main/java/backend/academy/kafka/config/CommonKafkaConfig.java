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
import org.apache.zookeeper.ZooKeeper;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaAdmin.NewTopics;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class CommonKafkaConfig {

    private final UserEventsTopicProperties userEventsTopicProperties;
    private final KafkaProperties kafkaProperties;

    @Bean
    Admin localKafkaClusterAdminClint() {
        return AdminClient.create(
            Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaProperties.getBootstrapServers()));
    }

    @Bean
    KafkaAdmin localKafkaClusterAdmin() {
        return new KafkaAdmin(
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
    NewTopics userEventsTopic() {
        return userEventsTopicProperties.toNewTopics();
    }

}
