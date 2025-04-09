package backend.academy.kafka.config;

import static backend.academy.kafka.config.KafkaStreamsConfig.Processors.DLQ_SINK_PROCESSOR;
import static backend.academy.kafka.config.KafkaStreamsConfig.Processors.ENRICHMENT_PROCESSOR;
import static backend.academy.kafka.config.KafkaStreamsConfig.Processors.ERRORS_PROCESSOR;
import static backend.academy.kafka.config.KafkaStreamsConfig.Processors.SCORING_PROCESSOR;
import static backend.academy.kafka.config.KafkaStreamsConfig.Processors.SINK_PROCESSOR;
import static backend.academy.kafka.config.KafkaStreamsConfig.Processors.SOURCE_PROCESSOR;
import static backend.academy.kafka.config.KafkaStreamsConfig.Processors.VALIDATION_PROCESSOR;
import backend.academy.kafka.config.properties.UserEventsTopicProperties;
import backend.academy.kafka.processor.EnrichmentProcessor;
import backend.academy.kafka.processor.ErrorProcessor;
import backend.academy.kafka.processor.ScoringProcessor;
import backend.academy.kafka.processor.ValidationProcessor;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;


@Configuration
@RequiredArgsConstructor
public class KafkaStreamsConfig {

    private final UserEventsTopicProperties userEventsTopicProperties;
    private final KafkaProperties kafkaProperties;

    private final ApplicationContext context;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kafkaStreamsConfig() {
        Map<String, Object> props = kafkaProperties.buildStreamsProperties(null);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "tx-fraud-prevention-service");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public Topology buildKafkaStreamsTopology() {
        var builder = new Topology();
        builder.addSource(SOURCE_PROCESSOR, userEventsTopicProperties.getInputTopic())
            .addProcessor(
                VALIDATION_PROCESSOR,
                () -> context.getBean(VALIDATION_PROCESSOR, ValidationProcessor.class),
                SOURCE_PROCESSOR
            )
            .addProcessor(
                ERRORS_PROCESSOR,
                () -> context.getBean(ERRORS_PROCESSOR, ErrorProcessor.class),
                VALIDATION_PROCESSOR
            )
            .addProcessor(
                ENRICHMENT_PROCESSOR,
                () -> context.getBean(ENRICHMENT_PROCESSOR, EnrichmentProcessor.class),
                VALIDATION_PROCESSOR
            )
            .addProcessor(
                SCORING_PROCESSOR,
                () -> context.getBean(SCORING_PROCESSOR, ScoringProcessor.class),
                ENRICHMENT_PROCESSOR
            )
            .addSink(DLQ_SINK_PROCESSOR, userEventsTopicProperties.getOutputTopic(), ERRORS_PROCESSOR)
            .addSink(SINK_PROCESSOR, userEventsTopicProperties.getOutputTopic(), SCORING_PROCESSOR);
        return builder;
    }

    @UtilityClass
    public static class Processors {

        public static final String SOURCE_PROCESSOR = "source-processor";
        public static final String VALIDATION_PROCESSOR = "validation-processor";
        public static final String ERRORS_PROCESSOR = "errors-processor";
        public static final String ENRICHMENT_PROCESSOR = "enrichment-processor";
        public static final String SCORING_PROCESSOR = "scoring-processor";
        public static final String SINK_PROCESSOR = "sink-processor";
        public static final String DLQ_SINK_PROCESSOR = "dlq-sink-processor";

    }

}
