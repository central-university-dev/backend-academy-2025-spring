package ru.tbank.sem12.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import ru.tbank.sem12.model.Hotels;
import ru.tbank.sem12.model.RoomTypes;

@Component
@Log4j2
public class HotelsApiClient {
    private static final String BASE_URL = "http://localhost:8080/hotelsApi";
    private static final String V_80_PERCENT_FAILURE_RATE = "v80percentFailureRate";
    private static final String V_30_SEC_OF_FAILURE_30_SE_OF_SUCCESS = "v30secOfFailure30seOfSuccess";

    @Retry(name = "getHotels")
    public Hotels getHotelsRetry() {
        return getHotels(V_80_PERCENT_FAILURE_RATE);
    }

    @Retry(name = "getRoomTypesByHotelId", fallbackMethod = "getRoomTypesByHotelIdFallback")
    public RoomTypes getRoomTypesByHotelIdRetryWithFallbackMethod(String hotelId) {
        return getRoomTypesByHotelId(V_80_PERCENT_FAILURE_RATE, hotelId);
    }

    @CircuitBreaker(name = "getHotels")
    public Hotels getHotelsCircuitBreaker() {
        return getHotels(V_30_SEC_OF_FAILURE_30_SE_OF_SUCCESS);

    }

    private Hotels getHotels(String apiVersion) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Hotels> response;

        try {
            response =
                restTemplate.getForEntity(BASE_URL + "/%s/hotels".formatted(apiVersion), Hotels.class);
            log.info("Ответ от API получен: статус = {}", response.getStatusCode());
        } catch (HttpServerErrorException e) {
            log.error("Ошибка при вызове API: статус = {}", e.getStatusCode());
            throw e;
        }
        return response.getBody();
    }

    private RoomTypes getRoomTypesByHotelIdFallback(String hotelId, Exception e) {
        return new RoomTypes(List.of());
    }

    private RoomTypes getRoomTypesByHotelId(String apiVersion, String hotelId) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<RoomTypes> response;

        try {
            response =
                restTemplate.getForEntity(BASE_URL + "/%s/hotels/%s/roomTypes".formatted(apiVersion, hotelId),
                    RoomTypes.class);
            log.info("Ответ от API получен: статус = {}", response.getStatusCode());
        } catch (HttpServerErrorException e) {
            log.error("Ошибка при вызове API: статус = {}", e.getStatusCode());
            throw e;
        }
        return response.getBody();
    }
}
