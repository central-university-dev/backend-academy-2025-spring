package ru.tbank.sem3.component.context_restarts_fixed;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.tbank.sem3.controller.MathOperationService;
import ru.tbank.sem3.external.CheckEvenRestService;

@SpringBootTest
public class BaseSpringBootMockingTest {
    @MockitoBean
    protected MathOperationService mathOperationService;
    @MockitoSpyBean
    protected CheckEvenRestService checkEvenRestService;
}
