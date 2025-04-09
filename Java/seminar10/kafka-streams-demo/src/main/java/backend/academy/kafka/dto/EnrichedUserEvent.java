package backend.academy.kafka.dto;

import static backend.academy.kafka.dto.UserEvent.UserEventType.WITHDRAWAL;
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
public class EnrichedUserEvent {

    private Long eventId;
    private Long userId;
    private Long accountId;
    private String accountNumber;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private Integer score;
    private boolean cached;

    public static EnrichedUserEvent from(UserEvent event, UserData data) {
        return new EnrichedUserEvent()
            .setEventId(event.getEventId())
            .setUserId(event.getUserId())
            .setAmount(
                event.getType() == WITHDRAWAL
                    ? event.getAmount().negate()
                    : event.getAmount())
            .setCreatedAt(event.getCreatedAt())
            .setAccountId(data.getAccountId())
            .setAccountNumber(data.getAccountNumber())
            .setScore(data.getScore());
    }

}
