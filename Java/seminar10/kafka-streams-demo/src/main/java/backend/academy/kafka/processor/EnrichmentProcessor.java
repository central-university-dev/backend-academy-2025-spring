package backend.academy.kafka.processor;

import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.ENRICHMENT_PROCESSOR;
import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.ENRICHMENT_STATE_STORE;
import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.ERRORS_PROCESSOR;
import static backend.academy.kafka.config.KafkaStreamsConfig.TopologyComponents.SCORING_PROCESSOR;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import backend.academy.kafka.dto.EnrichedUserEvent;
import backend.academy.kafka.dto.UserData;
import backend.academy.kafka.dto.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;


@Slf4j
@Scope(SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Component(ENRICHMENT_PROCESSOR)
public class EnrichmentProcessor implements Processor<String, String, String, String> {

    private static final String REASON_HEADER = "reason";

    private final ObjectMapper objectMapper;
    private final JdbcClient jdbcClient;

    private ProcessorContext<String, String> context;
    private KeyValueStore<Long, UserData> cache;

    @Override
    public void init(ProcessorContext<String, String> context) {
        this.context = context;
        this.cache = context.getStateStore(ENRICHMENT_STATE_STORE);
    }

    @Override
    @SneakyThrows
    public void process(Record<String, String> record) {
        log.info("Обогащаем запись: {}", record.value());

        var event = objectMapper.readValue(record.value(), UserEvent.class);
        var enrichedEvent = Optional.ofNullable(cache.get(event.getUserId()))
            .map(data -> EnrichedUserEvent.from(event, data).setCached(true))
            .or(() -> jdbcClient.sql(
                    """
                    select a.account_id, a.account_number, s.score from users u
                        join accounts a on u.account_id = a.account_id
                        left join account_scores s on s.account_id = a.account_id
                    where user_id = :userId
                    """)
                .param("userId", event.getUserId())
                .query((rs, __) -> {
                    var data = new UserData()
                        .setUserId(event.getUserId())
                        .setAccountId(rs.getLong(1))
                        .setAccountNumber(rs.getString(2))
                        .setScore(rs.getInt(3));
                    cache.putIfAbsent(event.getUserId(), data);
                    return EnrichedUserEvent.from(event, data);
                })
                .optional());

        if (enrichedEvent.isEmpty()) {
            context.forward(
                record.withHeaders(
                    new RecordHeaders(
                        List.of(
                            new RecordHeader(
                                REASON_HEADER,
                                "missing_data".getBytes())
                        )
                    )
                ),
                ERRORS_PROCESSOR
            );
            return;
        }

        context.forward(record.withValue(objectMapper.writeValueAsString(enrichedEvent.get())), SCORING_PROCESSOR);
    }

}
