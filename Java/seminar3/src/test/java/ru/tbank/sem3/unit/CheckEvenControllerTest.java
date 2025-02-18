package ru.tbank.sem3.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.sem3.controller.CheckEvenApiResponse;
import ru.tbank.sem3.controller.CheckEvenController;
import ru.tbank.sem3.controller.MathOperationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CheckEvenControllerTest {

    @Mock
    private MathOperationService mathOperationService;
    @InjectMocks
    private CheckEvenController checkEvenController;

    @Test
    public void checkEven__whenNumberIsNull_thenReturnEmptyResponse() {
        CheckEvenApiResponse emptyResponse = new CheckEvenApiResponse(null, null);
        when(mathOperationService.check(null)).thenReturn(emptyResponse);

        CheckEvenApiResponse result = checkEvenController.checkEven(null);

        assertEquals(emptyResponse, result);
    }
}
