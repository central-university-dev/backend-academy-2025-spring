package backend.academy.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ScoringResult {

    private Long eventId;
    private Integer score;
    private ScoringDecision decision;

    public enum ScoringDecision {
        APPROVED, NOT_APPROVED;
    }

}
