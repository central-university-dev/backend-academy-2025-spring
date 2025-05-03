package backend.academy.seminar13.reservation.client;

import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@HttpExchange(url = "/api", accept = APPLICATION_JSON_VALUE, contentType = APPLICATION_JSON_VALUE)
public interface HotelService {

    @GetExchange("/hotels")
    List<Hotel> getAllHotels();

    @GetExchange("/hotels/{id}/rooms")
    List<Room> getAllRoomsByHotelId(@PathVariable long id);
}
