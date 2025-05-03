package backend.academy.seminar13.hotel.domain;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends ListCrudRepository<Hotel, Long> {
}
