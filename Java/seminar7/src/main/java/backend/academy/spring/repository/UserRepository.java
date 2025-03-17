package backend.academy.spring.repository;

import backend.academy.spring.model.User;
import org.springframework.data.repository.ListCrudRepository;

public interface UserRepository extends ListCrudRepository<User, Long> {
}
