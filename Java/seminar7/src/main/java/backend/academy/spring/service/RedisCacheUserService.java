package backend.academy.spring.service;

import backend.academy.spring.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheUserService {
    public static final String USER_CACHE_PREFIX = "user:";
    private final UserService userService;
    private final RedisTemplate<String, User> redisTemplate;

    public User createUser(User user) {
        User newUser = userService.save(user);
        redisTemplate.opsForValue().set(USER_CACHE_PREFIX + newUser.getId(), newUser);
        return newUser;
    }

    public User findById(Long id) {
        User user = redisTemplate.opsForValue().get(USER_CACHE_PREFIX + id.toString());
        if (user != null) {
            log.info("User {} was found in cache", id);
            return user;
        }
        Optional<User> byId = userService.findById(id);
        byId.ifPresent(u -> redisTemplate.opsForValue().set(USER_CACHE_PREFIX + u.getId(), u));
        return byId.orElse(null);
    }

    public void deleteUser(Long id) {
        userService.deleteUser(id);
        log.info("Deleting user by id {} from cache", id);
        redisTemplate.delete(USER_CACHE_PREFIX + id);
    }
}
