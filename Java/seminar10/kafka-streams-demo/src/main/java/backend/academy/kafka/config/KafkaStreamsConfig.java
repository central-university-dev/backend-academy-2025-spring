package backend.academy.kafka.config;

import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.DLQ_SINK;
import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.ENRICHMENT_PROCESSOR;
import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.ENRICHMENT_STATE_STORE;
import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.ERRORS_PROCESSOR;
import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.SCORING_PROCESSOR;
import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.SINK;
import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.SOURCE;
import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.VALIDATION_PROCESSOR;
import backend.academy.kafka.config.properties.UserEventsTopicProperties;
import backend.academy.kafka.dto.UserData;
import backend.academy.kafka.processor.EnrichmentProcessor;
import backend.academy.kafka.processor.ErrorProcessor;
import backend.academy.kafka.processor.ScoringProcessor;
import backend.academy.kafka.processor.ValidationProcessor;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.SystemTime;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.internals.InMemoryKeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.internals.KeyValueStoreBuilder;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;


@Configuration
@RequiredArgsConstructor
public class KafkaStreamsConfig {

    private final UserEventsTopicProperties userEventsTopicProperties;
    private final KafkaProperties kafkaProperties;

    private final ApplicationContext context;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kafkaStreamsConfig() {
        Map<String, Object> props = kafkaProperties.buildStreamsProperties(null);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public Topology buildKafkaStreamsTopology() {
        var builder = new Topology();
        builder.addSource(SOURCE, userEventsTopicProperties.getInputTopic())
            .addProcessor(
                VALIDATION_PROCESSOR,
                () -> context.getBean(VALIDATION_PROCESSOR, ValidationProcessor.class),
                SOURCE
            )
            .addProcessor(
                ENRICHMENT_PROCESSOR,
                () -> context.getBean(ENRICHMENT_PROCESSOR, EnrichmentProcessor.class),
                VALIDATION_PROCESSOR
            )
            .addProcessor(
                ERRORS_PROCESSOR,
                () -> context.getBean(ERRORS_PROCESSOR, ErrorProcessor.class),
                VALIDATION_PROCESSOR, ENRICHMENT_PROCESSOR
            )
            .addProcessor(
                SCORING_PROCESSOR,
                () -> context.getBean(SCORING_PROCESSOR, ScoringProcessor.class),
                ENRICHMENT_PROCESSOR
            )
            .addStateStore(
                new KeyValueStoreBuilder<>(
                    new InMemoryKeyValueBytesStoreSupplier(ENRICHMENT_STATE_STORE),
                    Serdes.Long(),
                    Serdes.serdeFrom(
                        new JsonSerializer<>(),
                        new JsonDeserializer<>(UserData.class)
                    ),
                    new SystemTime()
                )
                    .withCachingEnabled()
                    .withLoggingEnabled(Map.of()),
                ENRICHMENT_PROCESSOR
            )
            .addSink(DLQ_SINK, userEventsTopicProperties.getDlqOutputTopic(), ERRORS_PROCESSOR)
            .addSink(SINK, userEventsTopicProperties.getOutputTopic(), SCORING_PROCESSOR);
        return builder;
    }

    @Slf4j
    @Component
    public static class KafkaStreamsTopologyInitializer implements AutoCloseable {

        private final KafkaStreams kafkaStreams;

        public KafkaStreamsTopologyInitializer(Topology topology, KafkaStreamsConfiguration config) {
            this.kafkaStreams = new KafkaStreams(topology, config.asProperties());
        }

        @EventListener(ApplicationReadyEvent.class)
        public void initTopology() {
            log.info("Инициализируем топологию Kafka Streams...");
            kafkaStreams.start();
        }

        @Override
        public void close() {
            kafkaStreams.close();
        }

    }

    @UtilityClass
    public static class TopologyComponents {

        public static final String SOURCE = "fp-source";
        public static final String VALIDATION_PROCESSOR = "fp-validation-processor";
        public static final String ERRORS_PROCESSOR = "fp-errors-processor";
        public static final String ENRICHMENT_PROCESSOR = "fp-enrichment-processor";
        public static final String SCORING_PROCESSOR = "fp-scoring-processor";
        public static final String SINK = "fp-sink";
        public static final String DLQ_SINK = "fp-dlq-sink";
        public static final String ENRICHMENT_STATE_STORE = "fp-enrichment-state-store";

    }

}
