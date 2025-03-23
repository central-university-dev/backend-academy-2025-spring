package backend.academy.kafka.model;

import java.time.LocalDateTime;
import lombok.Data;


@Data
public class UserEvent {

    private Long id;
    private Long userId;
    private UserEventType type;
    private LocalDateTime createdAt;

    public enum UserEventType {
        WITHDRAWAL, ACCRUAL;
    }

}
