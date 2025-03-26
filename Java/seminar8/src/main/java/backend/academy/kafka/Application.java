package backend.academy.kafka;

import backend.academy.kafka.service.UserEventsService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaRetryTopic;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableKafka
@EnableKafkaRetryTopic
@EnableScheduling
@SpringBootApplication
@RequiredArgsConstructor
public class Application {

    private final UserEventsService service;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationStartup() {
        service.sendMessages(30L, 10, true, false);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
