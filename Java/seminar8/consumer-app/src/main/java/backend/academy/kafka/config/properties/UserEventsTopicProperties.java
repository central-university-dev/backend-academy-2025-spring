package backend.academy.kafka.config.properties;

import lombok.Data;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;


@Data
@Configuration
@ConfigurationProperties("app.user-events")
public class UserEventsTopicProperties {

    private String topic;
    private int concurrency;

}
