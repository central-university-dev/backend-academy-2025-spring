package backend.academy.seminar13.reservation.client;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Room(
    long id,
    long roomTypeId,
    long hotelId,
    String name
) {
}
