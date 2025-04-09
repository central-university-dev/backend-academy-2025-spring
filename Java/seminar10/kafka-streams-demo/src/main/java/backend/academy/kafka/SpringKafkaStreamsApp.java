package backend.academy.kafka;

import backend.academy.kafka.service.UserEventsService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@EnableKafka
@EnableScheduling
@RequiredArgsConstructor
@SpringBootApplication
public class SpringKafkaStreamsApp {

    private final UserEventsService service;

    @Scheduled(initialDelay = 5, fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void sendData() {
        service.sendMessages(30L, 10);
        service.sendMessages(31L, 10);
        service.sendMessages(32L, 10);
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringKafkaStreamsApp.class, args);
    }

}
