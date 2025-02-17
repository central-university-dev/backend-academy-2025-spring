package ru.tbank.sem3.component.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@TestConfiguration
public class TestConfig {

    @Bean
    public RestClient restClient() {
        return Mockito.mock(RestClient.class);
    }
}
