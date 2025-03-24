package backend.academy.kafka.config;

import backend.academy.kafka.config.properties.UserEventsTopicProperties;
import backend.academy.kafka.consumer.UserEventsMessageListener;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;


@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;
    private final UserEventsTopicProperties topicProperties;
    private final UserEventsMessageListener userEventsMessageListener;

    @Bean
    public KafkaMessageListenerContainer<Long, byte[]> singleKafkaMessageListenerContainer() {
        var containerProperties = new ContainerProperties(topicProperties.getTopic());
        containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL);

        var factory = consumerFactory(props -> props.put(ConsumerConfig.GROUP_ID_CONFIG, "single-consumer"));
        var container = new KafkaMessageListenerContainer<>(factory, containerProperties);
        container.setupMessageListener(userEventsMessageListener);
        container.setChangeConsumerThreadName(true);
        container.setThreadNameSupplier(__ -> "single");
        container.setAutoStartup(true);
        return container;
    }

    @Bean
    public ConcurrentMessageListenerContainer<Long, byte[]> multiThreadedPartitionKafkaMessageListenerContainer() {
        var containerProperties = new ContainerProperties(topicProperties.getTopic());
        containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL);

        var factory = consumerFactory(props -> props.put(ConsumerConfig.GROUP_ID_CONFIG, "multi-consumer"));
        var container = new ConcurrentMessageListenerContainer<>(factory, containerProperties);
        container.setupMessageListener(userEventsMessageListener);
        container.setConcurrency(topicProperties.getPartitions()); // Note: try more!
        container.setChangeConsumerThreadName(true);
        container.setThreadNameSupplier(__ -> "multi");
        container.setAutoStartup(true);
        return container;
    }

    private ConsumerFactory<Long, byte[]> consumerFactory(Consumer<Map<String, Object>> propsModifier) {
        var props = kafkaProperties.buildConsumerProperties(null);

        // Serialization
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

        // Acks and offset seek
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        // Transactional
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        // Partitions
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());

        // Heartbeats
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 120_000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 1_000);

        // Batching
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1_000);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 1_000);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 1_000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);

        propsModifier.accept(props);
        return new DefaultKafkaConsumerFactory<>(props);
    }

}
