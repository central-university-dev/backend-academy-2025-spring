package backend.academy.kafka.config;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;


@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final KafkaProperties properties;

    @Bean
    @Primary
    public KafkaTemplate<Long, String> kafkaTemplate() {
        var props = properties.buildProducerProperties(null);

        // Serialization
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Acks
        props.put(ProducerConfig.ACKS_CONFIG, "0");
        // props.put(ProducerConfig.ACKS_CONFIG, "1");
        // props.put(ProducerConfig.ACKS_CONFIG, "all");

        // Batching
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10_000);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1_000);

        // Partitioning
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomUserPartitioner.class);

        // Transactional
        // props.put(ProducerConfig.ACKS_CONFIG, "all");
        // props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "app-tx-id");

        var factory = new DefaultKafkaProducerFactory<Long, String>(props);
        return new KafkaTemplate<>(factory);
    }

    @Slf4j
    public static class CustomUserPartitioner implements Partitioner {

        @Override
        public int partition(
            String topic, Object key, byte[] keyBytes,
            Object value, byte[] valueBytes, Cluster cluster
        ) {
            var userId = Optional.ofNullable(key)
                .filter(Long.class::isInstance)
                .map(Long.class::cast)
                .orElse(0L);
            var partition = (int) (userId % cluster.partitionCountForTopic(topic));
            log.info("Для пользователя с ИД {} была выбрана {} партиция", userId, partition);
            return partition;
        }

        @Override
        public void close() {

        }

        @Override
        public void configure(Map<String, ?> configs) {

        }

    }

}
