package backend.academy.kafka.config;

import backend.academy.kafka.config.properties.UserEventsTopicProperties;
import backend.academy.kafka.consumer.UserEventsMessageListener;
import backend.academy.kafka.model.UserEvent;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;


@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaProperties properties;
    private final UserEventsTopicProperties userEventsTopicProperties;
    private final UserEventsMessageListener userEventsMessageListener;

    @Bean("defaultConsumerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Long, UserEvent>> defaultConsumerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<Long, UserEvent>();
        factory.setConsumerFactory(
            consumerFactory(
                UserEventDeserializer.class,
                props -> props.put(ConsumerConfig.GROUP_ID_CONFIG, "default-consumer")));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(new CommonLoggingErrorHandler());
        factory.setAutoStartup(true);
        factory.setConcurrency(1);
        return factory;
    }

    @Bean("avroConsumerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Long, Object>> avroConsumerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<Long, Object>();
        factory.setConsumerFactory(
            consumerFactory(
                KafkaAvroDeserializer.class,
                props -> {
                    props.put(ConsumerConfig.GROUP_ID_CONFIG, "avro-consumer");
                    props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
                }));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(new CommonLoggingErrorHandler());
        factory.setAutoStartup(true);
        factory.setConcurrency(1);
        return factory;
    }

    @Bean
    public KafkaMessageListenerContainer<Long, byte[]> singleKafkaMessageListenerContainer() {
        var containerProperties = new ContainerProperties(userEventsTopicProperties.getTopic());
        containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL);

        var factory = consumerFactory(props -> props.put(ConsumerConfig.GROUP_ID_CONFIG, "single-consumer"));
        var container = new KafkaMessageListenerContainer<>(factory, containerProperties);
        container.setupMessageListener(userEventsMessageListener);
        container.setChangeConsumerThreadName(true);
        container.setThreadNameSupplier(c -> getThreadName("single", c));
        container.setAutoStartup(true);
        return container;
    }

    @Bean
    public ConcurrentMessageListenerContainer<Long, byte[]> multiThreadedPartitionKafkaMessageListenerContainer() {
        var containerProperties = new ContainerProperties(userEventsTopicProperties.getTopic());
        containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL);

        var factory = consumerFactory(props -> props.put(ConsumerConfig.GROUP_ID_CONFIG, "multi-consumer"));
        var container = new ConcurrentMessageListenerContainer<>(factory, containerProperties);
        container.setupMessageListener(userEventsMessageListener);
        container.setConcurrency(userEventsTopicProperties.getConcurrency()); // Note: try more!
        container.setChangeConsumerThreadName(true);
        container.setThreadNameSupplier(c -> getThreadName("multi", c));
        container.setAutoStartup(true);
        return container;
    }

    @Bean
    public KafkaTemplate<Long, UserEvent> userEventKafkaTemplate() {
        var props = properties.buildProducerProperties(null);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, UserEventSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "0");
        var factory = new DefaultKafkaProducerFactory<Long, UserEvent>(props);
        return new KafkaTemplate<>(factory);
    }

    private ConsumerFactory<Long, byte[]> consumerFactory(Consumer<Map<String, Object>> propsModifier) {
        return consumerFactory(ByteArrayDeserializer.class, propsModifier);
    }

    private <M> ConsumerFactory<Long, M> consumerFactory(
        Class<? extends Deserializer<M>> valueDeserializerClass,
        Consumer<Map<String, Object>> propsModifier
    ) {
        var props = properties.buildConsumerProperties(null);

        // Serialization
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerClass);

        // Partitions
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());

        propsModifier.accept(props);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    private static String getThreadName(String prefix, MessageListenerContainer c) {
        return prefix + "_" + c.getListenerId();
    }

    public static class UserEventDeserializer extends JsonDeserializer<UserEvent> {

    }

    public static class UserEventSerializer extends JsonSerializer<UserEvent> {

    }

}
