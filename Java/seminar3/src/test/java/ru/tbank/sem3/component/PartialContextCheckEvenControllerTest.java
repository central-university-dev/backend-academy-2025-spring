package ru.tbank.sem3.component;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.tbank.sem3.component.config.TestConfig;
import ru.tbank.sem3.controller.CheckEvenController;
import ru.tbank.sem3.controller.MathOperationService;
import ru.tbank.sem3.external.CheckEvenRestService;

@SpringBootTest(classes = {
        CheckEvenController.class,
        MathOperationService.class,
        CheckEvenRestService.class
})
@Import(TestConfig.class)
public class PartialContextCheckEvenControllerTest {

    @Test
    public void test() {

    }

}
