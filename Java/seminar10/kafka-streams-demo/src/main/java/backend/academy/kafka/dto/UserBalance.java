package backend.academy.kafka.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UserBalance {

    private Long userId;
    private BigDecimal balance;
    private List<BigDecimal> transactions;

    public UserBalance addTransaction(BigDecimal amount) {
        if (transactions == null) {
            transactions = new ArrayList<>();
        }
        transactions.add(amount);
        return this;
    }

}
