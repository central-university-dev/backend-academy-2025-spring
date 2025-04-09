package backend.academy.kafka.processor;

import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.SCORING_PROCESSOR;
import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.SINK;
import static backend.academy.kafka.dto.ScoringResult.ScoringDecision.APPROVED;
import static backend.academy.kafka.dto.ScoringResult.ScoringDecision.NOT_APPROVED;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import backend.academy.kafka.dto.EnrichedUserEvent;
import backend.academy.kafka.dto.ScoringResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Slf4j
@Scope(SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Component(SCORING_PROCESSOR)
public class ScoringProcessor implements Processor<String, String, String, String> {

    private final ObjectMapper objectMapper;

    private ProcessorContext<String, String> context;

    @Override
    public void init(ProcessorContext<String, String> context) {
        this.context = context;
    }

    @Override
    @SneakyThrows
    public void process(Record<String, String> record) {
        log.info("Производим скоринг записи: {}", record.value());

        var event = objectMapper.readValue(record.value(), EnrichedUserEvent.class);

        int totalScore = event.getScore();
        if (event.getAmount().abs().compareTo(BigDecimal.valueOf(500L)) >= 0) {
            totalScore += 30;
        }
        if (event.getCreatedAt().isBefore(LocalDateTime.now().minusSeconds(1))) {
            totalScore += 30;
        }
        if (event.getUserId() > 31) {
            totalScore += 30;
        }
        if (event.getAccountId() % 2 == 0) {
            totalScore += 30;
        }
        if (event.getAccountNumber().startsWith("acc")) {
            totalScore += 30;
        }
        totalScore *= System.currentTimeMillis() % 2 == 0 ? 10 : 1;

        var scoringResult = new ScoringResult()
            .setEventId(event.getEventId())
            .setScore(totalScore)
            .setDecision(totalScore > 500 ? APPROVED : NOT_APPROVED);
        context.forward(record.withValue(objectMapper.writeValueAsString(scoringResult)), SINK);
    }

}
