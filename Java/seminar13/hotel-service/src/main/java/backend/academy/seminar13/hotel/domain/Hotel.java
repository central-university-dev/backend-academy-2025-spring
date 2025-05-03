package backend.academy.seminar13.hotel.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Table("hotels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class Hotel {

    @Id
    @JsonProperty(access = READ_ONLY)
    private long id;

    @NotBlank
    private String name;

    @NotBlank
    private String address;
}
