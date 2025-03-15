package backend.academy.spring.service;

import backend.academy.spring.model.User;
import backend.academy.spring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User save(User user) {
        log.info("Creating/updating user {} in DB", user);
        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        log.info("Finding user by id {} in DB", id);
        return userRepository.findById(id);
    }

    public void deleteUser(Long id) {
        log.info("Deleting user by id {} from DB", id);
        userRepository.deleteById(id);
    }
}
