package backend.academy.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UserEvent {

    private Long eventId;
    private Long userId;
    private BigDecimal amount;
    private UserEventType type;
    private LocalDateTime createdAt;

    public enum UserEventType {
        WITHDRAWAL, ACCRUAL;
    }

}
