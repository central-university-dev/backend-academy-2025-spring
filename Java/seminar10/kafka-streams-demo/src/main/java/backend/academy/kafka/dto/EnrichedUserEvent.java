package backend.academy.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class EnrichedUserEvent {

    private Long eventId;
    private Long userId;
    private Long accountId;
    private BigDecimal amount;
    private UserEventType type;
    private LocalDateTime createdAt;
    private Integer score;

    public enum UserEventType {
        WITHDRAWAL, ACCRUAL;
    }

}
