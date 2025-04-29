package ru.tbank.sem12.controller;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.tbank.sem12.client.HotelsApiClient;
import ru.tbank.sem12.model.Hotels;
import ru.tbank.sem12.model.RoomTypes;
import ru.tbank.sem12.model.Statistics;
import static java.lang.Thread.sleep;

@SpringBootApplication
@RestController
@RequiredArgsConstructor
@Log4j2
public class ApiGatewayController {

    private final HotelsApiClient hotelsApiClient;

    @GetMapping("/apiGateway/vRetry/hotels")
    public Hotels getHotelsRetry() {
        return hotelsApiClient.getHotelsRetry();
    }

    @GetMapping("/apiGateway/vRetryWithFallbackMethod/hotels/{hotelId}/roomTypes")
    public RoomTypes getRoomTypesByHotelId(@PathVariable String hotelId) {
        return hotelsApiClient.getRoomTypesByHotelIdRetryWithFallbackMethod(hotelId);
    }

    @SneakyThrows @GetMapping("/apiGateway/vCircuitBreaker/hotels")
    public Hotels getHotelsVCircuitBreaker() {
        try {
            return hotelsApiClient.getHotelsCircuitBreaker();
        } catch (Exception e) {
            sleep(100);
            return getHotelsVCircuitBreaker();
        }
    }

    @SneakyThrows @GetMapping("/apiGateway/vBulkhead/statistics")
    @Bulkhead(name = "getStatistics")
    public ResponseEntity<Statistics> getStatisticsBulkhead() {
        log.info("Executing getStatisticsBulkhead");
        sleep(10_000);
        return ResponseEntity.ok(new Statistics(
            4, 120, 50, 70, 12_000_00L
        ));
    }

    @ExceptionHandler(BulkheadFullException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public void handleBulkheadFullException() {
        log.warn("Handing bulkheadFullException");
    }
}
