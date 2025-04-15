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
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;


@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

    public static final String GENERIC_KAFKA_TEMPLATE_BEAN = "genericKafkaTemplate";
    private final KafkaProperties properties;

    @Bean(GENERIC_KAFKA_TEMPLATE_BEAN)
    public KafkaTemplate<Long, String> genericKafkaTemplate() {
        var props = properties.buildProducerProperties(null);

        // Serialization
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Partitioning
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomUserPartitioner.class);

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
            return (int) (userId % cluster.partitionCountForTopic(topic));
        }

        @Override
        public void close() {

        }

        @Override
        public void configure(Map<String, ?> configs) {

        }

    }

}
