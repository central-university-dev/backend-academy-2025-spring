package backend.academy.kafka.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;


@Data
public class User {

    private Long id;
    private String name;
    private BigDecimal balance;
    private LocalDateTime createdAt;

}
