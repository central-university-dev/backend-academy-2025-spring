package backend.academy;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SpringBootApp {

    private final ApplicationContext context;

    public SpringBootApp(ApplicationContext context) {
        this.context = context;
    }

    @PostConstruct
    public void init() {
        context.getBeanDefinitionNames();
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootApp.class, args);
    }
}
