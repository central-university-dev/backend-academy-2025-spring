package backend.academy.kafka.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CustomUserEvent {

    private Long id;
    private Long userId;
    private CustomUserEventType type;
    private LocalDateTime createdAt;

    public enum CustomUserEventType {
        WITHDRAWAL, ACCRUAL;
    }

}
