package ru.tbank.sem3.component;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.tbank.sem3.controller.MathOperationService;
import ru.tbank.sem3.external.CheckEvenRestService;
import ru.tbank.sem3.external.model.CheckEvenResponse;

import static org.mockito.Mockito.when;

@SpringBootTest
public class FullContextCheckEvenControllerTest {

    @MockitoBean
    private CheckEvenRestService checkEvenRestService;
    @MockitoSpyBean
    private MathOperationService mathOperationService;

    @Test
    public void test() {
        //...
        when(checkEvenRestService.checkEven(10))
                .thenReturn(new CheckEvenResponse(true, ""));
        //...
    }
}



