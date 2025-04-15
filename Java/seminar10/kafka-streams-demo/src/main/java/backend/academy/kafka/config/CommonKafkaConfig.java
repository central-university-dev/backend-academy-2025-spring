package backend.academy.kafka.config;

import backend.academy.kafka.config.properties.UserEventsTopicProperties;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaAdmin.NewTopics;


@Slf4j
@EnableKafka
@Configuration
@RequiredArgsConstructor
public class CommonKafkaConfig {

    private final UserEventsTopicProperties userEventsTopicProperties;
    private final KafkaProperties kafkaProperties;

    @Bean
    KafkaAdmin localKafkaClusterAdmin() {
        return new KafkaAdmin(
            Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaProperties.getBootstrapServers()));
    }

    @Bean
    @SneakyThrows
    NewTopics userEventsTopic() {
        return userEventsTopicProperties.toNewTopics();
    }

}
