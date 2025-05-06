package backend.academy.seminar13.reservation;

import backend.academy.seminar13.reservation.domain.Reservation;
import backend.academy.seminar13.reservation.domain.ReservationRepository;
import backend.academy.seminar13.reservation.domain.RoomTypeInventory;
import backend.academy.seminar13.reservation.domain.RoomTypeInventoryRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final RoomTypeInventoryRepository roomTypeInventoryRepository;

    @GetMapping("/guests/{id}/reservations")
    public List<Reservation> getAllReservationsByGuestId(@PathVariable long id) {
        return reservationRepository.findAllByGuestId(id)
            .stream()
            .sorted(Comparator.comparingLong(Reservation::getId))
            .toList();
    }

    @GetMapping("/inventory")
    public List<RoomTypeInventory> getAllInventory(@RequestParam Optional<LocalDate> fromDate) {
        List<RoomTypeInventory> result;
        if (fromDate.isPresent()) {
            result = roomTypeInventoryRepository.findAllStartingFrom(fromDate.get());
        } else {
            result = roomTypeInventoryRepository.findAll();
        }

        return result.stream()
            .sorted(Comparator.comparing(RoomTypeInventory::getHotelId)
                .thenComparing(RoomTypeInventory::getRoomTypeId)
                .thenComparing(RoomTypeInventory::getDate))
            .toList();
    }

    @PostMapping("/reservations")
    @Transactional
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservation) {
        List<RoomTypeInventory> inventoryList = roomTypeInventoryRepository.findAllByHotelIdAndRoomTypeId(
            reservation.getHotelId(),
            reservation.getRoomTypeId(),
            reservation.getStartDate(),
            reservation.getEndDate()
        );

        Map<LocalDate, RoomTypeInventory> inventoryByDate = inventoryList.stream()
            .collect(Collectors.toMap(RoomTypeInventory::getDate, Function.identity()));

        List<LocalDate> reservationDates = reservation.getStartDate()
            .datesUntil(reservation.getEndDate().plusDays(1)).toList();

        for (var date : reservationDates) {
            RoomTypeInventory inventory = inventoryByDate.get(date);
            if (inventory == null || inventory.getTotalReserved() >= inventory.getTotalInventory()) {
                return ResponseEntity.notFound().build();
            }
        }

        for (var date : reservationDates) {
            RoomTypeInventory inventory = inventoryByDate.get(date);
            roomTypeInventoryRepository.updateReserved(inventory.getHotelId(), inventory.getRoomTypeId(),
                inventory.getDate(), inventory.getTotalReserved() + 1);
        }

        return ResponseEntity.ok(reservationRepository.save(reservation));
    }
}
