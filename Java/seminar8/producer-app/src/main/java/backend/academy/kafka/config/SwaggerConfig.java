package backend.academy.kafka.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collections;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().servers(Collections.singletonList(new Server().url("/")));
    }

    @Bean
    public GroupedOpenApi swaggerAll() {
        return GroupedOpenApi.builder()
            .group("all")
            .pathsToMatch("/**")
            .build();
    }

}
