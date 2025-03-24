package backend.academy.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaRetryTopic;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableKafka
@EnableKafkaRetryTopic
@EnableScheduling
@SpringBootApplication
@RequiredArgsConstructor
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
