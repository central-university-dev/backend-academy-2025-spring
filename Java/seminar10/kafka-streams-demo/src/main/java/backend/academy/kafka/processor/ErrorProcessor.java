package backend.academy.kafka.processor;

import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.DLQ_SINK;
import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.ERRORS_PROCESSOR;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Slf4j
@Scope(SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Component(ERRORS_PROCESSOR)
public class ErrorProcessor implements Processor<String, String, String, String> {

    private static final String REASON_HEADER = "reason";

    private ProcessorContext<String, String> context;

    @Override
    public void init(ProcessorContext<String, String> context) {
        this.context = context;
    }

    @Override
    @SneakyThrows
    public void process(Record<String, String> record) {
        var reason = Optional.ofNullable(record.headers().lastHeader(REASON_HEADER))
            .map(Header::value)
            .map(String::new)
            .orElse("unknown");
        log.error(
            "Следующее сообщение было пропущено по причине \"{}\": {}",
            reason, record.value()
        );

        context.forward(record, DLQ_SINK);
    }

}
