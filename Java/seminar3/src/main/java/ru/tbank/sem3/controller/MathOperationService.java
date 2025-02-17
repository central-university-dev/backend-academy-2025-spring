package ru.tbank.sem3.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.tbank.sem3.external.CheckEvenRestService;
import ru.tbank.sem3.external.model.CheckEvenResponse;

@Service
@RequiredArgsConstructor
public class MathOperationService {
    private static final String AD_BLOCKED_STRING = "BLOCKED";
    private final CheckEvenRestService checkEvenRestService;

    public CheckEvenApiResponse check(Integer number) {
        if (number == null) {
            throw new IllegalArgumentException("Number is null");
        }

        CheckEvenResponse response = checkEvenRestService.checkEven(number);
        return convertWithAdBlock(response);
    }

    private CheckEvenApiResponse convertWithAdBlock(CheckEvenResponse response) {
        String ad = response.ad().length() > 15 ? AD_BLOCKED_STRING : response.ad();
        return new CheckEvenApiResponse(response.iseven(), ad);
    }

    public Integer sum(Integer a, Integer b) {
        return a + b;
    }

    public Integer divide(Integer a, Integer b) {
        return a / b;
    }

    public Integer multiply(Integer a, Integer b) {
        return a * b;
    }
}
