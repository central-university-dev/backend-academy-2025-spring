package ru.tbank.sem3.external;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.tbank.sem3.external.model.CheckEvenResponse;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckEvenRestService {
    private final RestClient restClient;

    public CheckEvenResponse checkEven(Integer number) {
        return restClient.get()
                .uri("/iseven/{number}", Map.of("number", number))
                .retrieve()
                .toEntity(CheckEvenResponse.class)
                .getBody();
    }
}
