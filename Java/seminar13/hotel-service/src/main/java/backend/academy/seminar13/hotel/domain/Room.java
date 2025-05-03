package backend.academy.seminar13.hotel.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Table("rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Room {

    @Id
    @JsonProperty(access = READ_ONLY)
    private long id;

    @Min(1)
    private long roomTypeId;

    @Min(1)
    private long hotelId;

    @NotBlank
    private String name;
}
