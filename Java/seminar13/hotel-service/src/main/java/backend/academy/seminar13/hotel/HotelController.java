package backend.academy.seminar13.hotel;

import backend.academy.seminar13.hotel.domain.Hotel;
import backend.academy.seminar13.hotel.domain.HotelRepository;
import backend.academy.seminar13.hotel.domain.Room;
import backend.academy.seminar13.hotel.domain.RoomRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    @GetMapping("/hotels")
    public List<Hotel> getAllHotels(HttpServletRequest request) {
        log.atInfo()
            .addKeyValue("path", "/api/hotels")
            .addKeyValue("ip", request.getRemoteAddr())
            .log("Received controller request");

        return hotelRepository.findAll();
    }

    @GetMapping("/hotels/{id}/rooms")
    public List<Room> getAllRoomsByHotelId(@PathVariable Long id, HttpServletRequest request) {
        log.atInfo()

            .addKeyValue("path", "/api/hotels/{id}/rooms")
            .addKeyValue("ip", request.getRemoteAddr())
            .log("Received controller request");

        return roomRepository.findAllByHotelId(id);
    }
}
