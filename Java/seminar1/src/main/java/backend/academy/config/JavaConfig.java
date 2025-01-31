package backend.academy.config;

import backend.academy.beans.BasicComponent;
import backend.academy.beans.DependencyHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class JavaConfig {

    @Bean
    public BasicComponent basicComponent1() {
        return new BasicComponent("BasicComponent1");
    }

    @Bean
    @Primary
    public BasicComponent basicComponent2() {
        return new BasicComponent("BasicComponent2");
    }

    @Bean
    public DependencyHolder dependencyHolder() {
        return new DependencyHolder(basicComponent1());
    }

    @Bean
    public String debugMessage() {
        return "debug message";
    }

    @Bean
    public String anotherDebugMessage() {
        return "debug message";
    }
}
