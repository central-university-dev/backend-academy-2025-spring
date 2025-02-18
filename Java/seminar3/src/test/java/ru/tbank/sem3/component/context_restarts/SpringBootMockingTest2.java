package ru.tbank.sem3.component.context_restarts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.tbank.sem3.external.CheckEvenRestService;

@SpringBootTest
public class SpringBootMockingTest2 {
    @MockitoBean
    private CheckEvenRestService checkEvenService;

    @Test
    public void test() {

    }
}
