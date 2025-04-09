package backend.academy.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UserData {

    private Long userId;
    private Long accountId;
    private String accountNumber;
    private Integer score;

}
