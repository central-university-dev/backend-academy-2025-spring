package ru.tbank.sem3.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.sem3.controller.MathOperationService;
import ru.tbank.sem3.external.CheckEvenRestService;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class MathOperationServiceTest {

    @Mock
    private CheckEvenRestService checkEvenRestService;
    @InjectMocks
    private MathOperationService mathOperationService;

    @Test
    public void checkEven__whenNumberIsNull_thenThrowException() {
        assertThrows(IllegalArgumentException.class, () -> mathOperationService.check(null));
    }

    @Test
    public void checkEven__whenAdIsLongerThen15Characters_thenBlockAd() {
        // написать на семинаре
    }

    @Test
    public void checkEven__whenAdIsShoterThen15Characters_thenBlockAd() {
        // написать на семинаре
    }
}
