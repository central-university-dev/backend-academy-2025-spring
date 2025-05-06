package backend.academy.seminar13.reservation.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Table("reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Reservation {

    @Id
    @JsonProperty(access = READ_ONLY)
    private long id;

    @Min(1)
    private long hotelId;

    @Min(1)
    private long roomTypeId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @Min(1)
    private long guestId;
}
