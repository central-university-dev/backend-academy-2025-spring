package ru.tbank.sem3.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CheckEvenController {
    private final MathOperationService mathOperationService;

    @GetMapping("/isEven")
    // .../isEven?number=2
    public CheckEvenApiResponse checkEven(
            @RequestParam(required = false) Integer number) {
        return mathOperationService.check(number);
    }

    @GetMapping("/sum")
    public Integer sum(@RequestParam Integer a, @RequestParam Integer b) {
        return mathOperationService.sum(a, b);
    }

    @GetMapping("/multiply")
    public Integer multiply(@RequestParam Integer a, @RequestParam Integer b) {
        return mathOperationService.multiply(a, b);
    }

    @GetMapping("/divide")
    public Integer divide(@RequestParam Integer a, @RequestParam Integer b) {
        return mathOperationService.divide(a, b);
    }
}
