package ru.tbank.sem12.controller;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.sem12.model.Hotel;
import ru.tbank.sem12.model.Hotels;
import ru.tbank.sem12.model.RoomType;
import ru.tbank.sem12.model.RoomTypes;

@SpringBootApplication
@RestController
@RequiredArgsConstructor
@Configuration
@Log4j2
public class HotelsApiController {

    private Map<String, Hotel> hotelIdToHotel = Map.of(
        "1", new Hotel("1", "Hotel Astoria", "St. Petersburg, Russia"),
        "2", new Hotel("2", "Hotel Ritz-Carlton", "Moscow, Russia"),
        "3", new Hotel("3", "InterContinental", "Almaty, Kazakhstan"),
        "4", new Hotel("4", "Hotel Europe", "Minsk, Belarus")
    );

    private Map<String, List<RoomType>> hotelIdToRoomTypes = Map.of(
        "1", List.of(
            new RoomType("1", "Single", List.of("TV", "Wifi", "Mini-bar")),
            new RoomType("2", "Double", List.of("TV", "Wifi", "Mini-bar", "Air conditioning"))
        ),
        "2", List.of(
            new RoomType("3", "Deluxe", List.of("TV", "Wifi", "Spa access", "City view")),
            new RoomType("4", "Suite", List.of("TV", "Wifi", "Private balcony", "Butler service"))
        ),
        "3", List.of(
            new RoomType("5", "Standard", List.of("TV", "Wifi")),
            new RoomType("6", "Executive", List.of("TV", "Wifi", "Mountain view", "Free breakfast"))
        ),
        "4", List.of(
            new RoomType("7", "Single", List.of("TV", "Wifi")),
            new RoomType("8", "Luxury Suite", List.of("TV", "Wifi", "Sauna", "Jacuzzi"))
        )
    );

    @GetMapping("/hotelsApi/v80percentFailureRate/hotels")
    public ResponseEntity<Hotels> getHotelsV80percentFailureRate() {
        log.info("Executing getHotelsV80percentFailureRate");
        Random random = new Random();
        int chance = random.nextInt(100);
        if (chance < 80) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        List<Hotel> hotels = hotelIdToHotel.values().stream().toList();
        return ResponseEntity.ok(new Hotels(hotels));
    }

    @GetMapping("/hotelsApi/v80percentFailureRate/hotels/{hotelId}/roomTypes")
    public ResponseEntity<RoomTypes> getRoomTypesByHotelIdV80percentFailureRate(@PathVariable String hotelId) {
        log.info("Executing getRoomTypesByHotelIdV80percentFailureRate");
        Random random = new Random();
        int chance = random.nextInt(100);
        if (chance < 80) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        List<RoomType> roomTypes = hotelIdToRoomTypes.getOrDefault(hotelId, List.of());
        return ResponseEntity.ok(new RoomTypes(roomTypes));
    }

    @GetMapping("/hotelsApi/v30secOfFailure30seOfSuccess/hotels")
    public ResponseEntity<Hotels> getHotelsV30secOfFailure30seOfSuccess() {
        log.info("Executing getHotelsV30secOfFailure30seOfSuccess");
        int currentSecond = LocalDateTime.now().getSecond();
        if (currentSecond <= 31) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        List<Hotel> hotels = hotelIdToHotel.values().stream().toList();
        return ResponseEntity.ok(new Hotels(hotels));
    }

    @GetMapping("/hotelsApi/vRateLimiter/hotels")
    @RateLimiter(name = "getHotelsVRateLimiter")
    public ResponseEntity<Hotels> getHotelsVRateLimiter() {
        log.info("Executing getHotelsVRateLimiter");
        List<Hotel> hotels = hotelIdToHotel.values().stream().toList();
        return ResponseEntity.ok(new Hotels(hotels));
    }

    @ExceptionHandler(RequestNotPermitted.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public void handleRequestNotPermitted() {
        log.warn("Handing requestNotPermitted");
    }
}
