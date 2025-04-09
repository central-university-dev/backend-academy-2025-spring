package backend.academy.kafka.processor;

import static backend.academy.kafka.config.KafkaStreamsConfig.Processors.ENRICHMENT_PROCESSOR;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.Record;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Scope(SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Component(ENRICHMENT_PROCESSOR)
public class EnrichmentProcessor implements Processor<String, String, String, String> {

    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @SneakyThrows
    public void process(Record<String, String> record) {

    }

}
