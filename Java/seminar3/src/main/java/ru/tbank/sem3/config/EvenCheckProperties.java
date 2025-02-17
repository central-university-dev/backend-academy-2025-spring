package ru.tbank.sem3.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "even.checker")
public class EvenCheckProperties {
    @NotBlank
    private String url;
}
