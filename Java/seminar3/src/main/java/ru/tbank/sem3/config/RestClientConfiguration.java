package ru.tbank.sem3.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(EvenCheckProperties.class)
public class RestClientConfiguration {

    @Bean
    public RestClient evenCheckerRestClient(EvenCheckProperties evenCheckProperties) {
        return RestClient.builder()
                .baseUrl(evenCheckProperties.getUrl())
                .build();
    }
}
