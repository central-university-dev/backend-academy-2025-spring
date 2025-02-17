package ru.tbank.sem3.component.context_restarts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.tbank.sem3.controller.MathOperationService;
import ru.tbank.sem3.external.CheckEvenRestService;

@SpringBootTest
public class SpringBootMockingTest1 {
    @MockitoBean
    private MathOperationService mathOperationService;
    @MockitoBean
    private CheckEvenRestService checkEvenRestService;

    @Test
    public void test() {

    }
}
