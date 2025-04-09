package backend.academy.kafka.processor;

import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.ENRICHMENT_PROCESSOR;
import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.ERRORS_PROCESSOR;
import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.VALIDATION_PROCESSOR;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import backend.academy.kafka.dto.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;


@Slf4j
@Scope(SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Component(VALIDATION_PROCESSOR)
public class ValidationProcessor implements Processor<String, String, String, String> {

    private static final String REASON_HEADER = "reason";

    private final ObjectMapper objectMapper;
    private final Validator validator;

    private ProcessorContext<String, String> context;

    @Override
    public void init(ProcessorContext<String, String> context) {
        this.context = context;
    }

    @Override
    @SneakyThrows
    public void process(Record<String, String> record) {
        log.info("Валидируем запись: {}", record.value());
        try {
            var user = objectMapper.readValue(record.value(), UserEvent.class);
            var violations = validator.validateObject(user);
            if (violations.hasErrors()) {
                context.forward(
                    record.withHeaders(
                        new RecordHeaders(
                            List.of(
                                new RecordHeader(
                                    REASON_HEADER,
                                    "validation".getBytes())
                            )
                        )
                    ),
                    ERRORS_PROCESSOR
                );
            }
        } catch (Throwable throwable) {
            context.forward(
                record.withHeaders(
                    new RecordHeaders(
                        List.of(
                            new RecordHeader(
                                REASON_HEADER,
                                "deserialization".getBytes())
                        )
                    )
                ),
                ERRORS_PROCESSOR
            );
        }
        context.forward(record, ENRICHMENT_PROCESSOR);
    }

}
