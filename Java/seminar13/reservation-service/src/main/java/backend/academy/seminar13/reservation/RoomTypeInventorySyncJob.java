package backend.academy.seminar13.reservation;

import backend.academy.seminar13.reservation.client.Hotel;
import backend.academy.seminar13.reservation.client.HotelService;
import backend.academy.seminar13.reservation.client.Room;
import backend.academy.seminar13.reservation.domain.RoomTypeInventory;
import backend.academy.seminar13.reservation.domain.RoomTypeInventoryRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
@Endpoint(id = "inventorySync")
public class RoomTypeInventorySyncJob {

    private static final int SYNC_DAYS = 10;

    private final AtomicBoolean isEnabled = new AtomicBoolean(true);

    private final HotelService hotelService;
    private final RoomTypeInventoryRepository repository;
    private final TransactionTemplate transactionTemplate;

    @ReadOperation
    public Map<String, Object> isEnabled() {
        return Map.of(
            "isEnabled", isEnabled.get(),
            "description", "Whether to enable background room type inventory"
        );
    }

    @WriteOperation
    public String setEnabled(@Selector String isEnabled) {
        var toEnable = Boolean.parseBoolean(isEnabled);
        var wasEnabled = this.isEnabled.getAndSet(toEnable);
        if (toEnable) {
            return wasEnabled ? "Job is already enabled" : "Job is now enabled";
        } else {
            return !wasEnabled ? "Job is already disabled" : "Job is now disabled";
        }
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void syncRoomTypeInventory() {
        if (!isEnabled.get()) {
            log.info("Room type inventory sync is disabled");
            return;
        }

        log.info("Started sync room type inventory process");

        LocalDate today = LocalDate.now();

        List<RoomTypeInventory> currentInventory = repository.findAllStartingFrom(today);

        List<LocalDate> missedDates = new ArrayList<>(today.datesUntil(today.plusDays(SYNC_DAYS)).toList());

        currentInventory.stream()
            .map(RoomTypeInventory::getDate)
            .forEach(missedDates::remove);

        if (!missedDates.isEmpty()) {
            List<RoomsGroup> roomsGroups = getAvailableRoomsByHotelAndRoomType();
            transactionTemplate.executeWithoutResult(ignore -> {
                for (var missedDate : missedDates) {
                    for (var roomsGroup : roomsGroups) {
                        persistRoomGroupAsInventory(roomsGroup, missedDate);
                    }
                }
            });
        }

        log.info("Finished sync room type inventory process");
    }

    private List<RoomsGroup> getAvailableRoomsByHotelAndRoomType() {
        List<Hotel> hotels = hotelService.getAllHotels();
        List<Room> rooms = new ArrayList<>();

        hotels.stream()
            .map(Hotel::id)
            .map(hotelService::getAllRoomsByHotelId)
            .forEach(rooms::addAll);

        return rooms.stream()
            .collect(Collectors.groupingBy(Room::hotelId))
            .entrySet()
            .stream()
            .map(entry -> new RoomsGroup(
                entry.getKey(),
                entry.getValue().stream().collect(Collectors.groupingBy(Room::roomTypeId))
            ))
            .toList();
    }

    private void persistRoomGroupAsInventory(RoomsGroup roomsGroup, LocalDate inventoryDate) {
        for (var entry : roomsGroup.roomsByTypeId().entrySet()) {
            repository.save(roomsGroup.hotelId(), entry.getKey(), inventoryDate, entry.getValue().size());
        }
    }

    private record RoomsGroup(long hotelId, Map<Long, List<Room>> roomsByTypeId) {
    }
}
