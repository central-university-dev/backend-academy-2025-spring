package backend.academy.seminar13.hotel.domain;

import java.util.List;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends ListCrudRepository<Room, Long> {

    List<Room> findAllByHotelId(long hotelId);
}
