package backend.academy.spring;

import backend.academy.spring.model.User;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import backend.academy.spring.service.LocalCacheUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@Slf4j
@SpringBootApplication
@EnableCaching
public class SpringCache implements CommandLineRunner {

    @Autowired
    private LocalCacheUserService cachableUserService;

    public static void main(String[] args) {
        SpringApplication.run(SpringCache.class, args);
    }

    @Override
    public void run(String... args) {
        Set<Long> ids = new HashSet<>();
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            long randomNum = random.nextLong(10000);
            User user = cachableUserService.createUser(
                new User(null, "user" + randomNum + "@example.com", "Name-" + randomNum));
            ids.add(user.getId());
        }
        Long toUpdateId = ids.iterator().next();
        User userToUpdate = cachableUserService.findById(toUpdateId);
        log.info("User before update:{}", userToUpdate);
        userToUpdate.setEmail("newEmail@example.com");
        cachableUserService.updateUser(userToUpdate);
        User updatedUser = cachableUserService.findById(toUpdateId);
        log.info("User after update:{}", updatedUser);

        log.info("clearing db and cache");
        for (Long id : ids) {
            cachableUserService.deleteUser(id);
        }
    }
}
