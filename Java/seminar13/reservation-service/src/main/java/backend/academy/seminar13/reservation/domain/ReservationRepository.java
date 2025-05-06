package backend.academy.seminar13.reservation.domain;

import java.util.List;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends ListCrudRepository<Reservation, Long> {

    List<Reservation> findAllByGuestId(long guestId);
}
