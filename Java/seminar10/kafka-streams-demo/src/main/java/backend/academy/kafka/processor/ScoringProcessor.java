package backend.academy.kafka.processor;

import static backend.academy.kafka.config.KafkaStreamsConfig.Processors.SCORING_PROCESSOR;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.Record;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Slf4j
@Scope(SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Component(SCORING_PROCESSOR)
public class ScoringProcessor implements Processor<String, String, String, String> {

    @Override
    @SneakyThrows
    public void process(Record<String, String> record) {

    }

}
