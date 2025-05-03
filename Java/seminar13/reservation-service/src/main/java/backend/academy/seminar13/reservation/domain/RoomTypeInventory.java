package backend.academy.seminar13.reservation.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.relational.core.mapping.Table;

@Table("room_type_inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@JsonNaming(SnakeCaseStrategy.class)
public class RoomTypeInventory {

    @Min(1)
    private long hotelId;

    @Min(1)
    private long roomTypeId;

    @NotNull
    private LocalDate date;

    @Min(0)
    private int totalInventory;

    @Min(0)
    private int totalReserved;
}
