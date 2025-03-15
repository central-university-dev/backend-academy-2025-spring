package backend.academy.spring.service;

import backend.academy.spring.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalCacheUserService {

    private final UserService userService;

    //cache aside
    @Cacheable(value = "users", key = "#id")
    public User findById(Long id) {
        log.info("Getting user by id: " + id);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return userService.findById(id).orElse(null);
    }

    @CachePut(value = "users", key = "#user.id")
    public User createUser(User user) {
        log.info("Creating user with email: " + user.getEmail());
        return userService.save(user);
    }

    @CachePut(value = "users", key = "#user.id")
    public User updateUser(User user) {
        log.info("Updating user with id: " + user.getId());
        return userService.save(user);
    }

    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        log.info("Deleting user with id: " + id);
        userService.deleteUser(id);
    }
}
