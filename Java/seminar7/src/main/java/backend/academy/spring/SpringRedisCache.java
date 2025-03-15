package backend.academy.spring;

import backend.academy.spring.model.User;
import backend.academy.spring.service.RedisCacheUserService;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@Slf4j
//@SpringBootApplication
@EnableCaching
public class SpringRedisCache implements CommandLineRunner {

    @Autowired
    private RedisCacheUserService redisCacheUserService;

    public static void main(String[] args) {
        SpringApplication.run(SpringRedisCache.class, args);
    }

    @Override
    public void run(String... args) {
        Set<Long> ids = new HashSet<>();
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            long randomNum = random.nextLong(10000);
            User user = redisCacheUserService.createUser(
                new User(null, "user" + randomNum + "@example.com", "Name-" + randomNum));
            ids.add(user.getId());
        }

        log.info("Searching non existing user by id=0L. user={}", redisCacheUserService.findById(0L));

        log.info("Searching existing users");
        for (Long id : ids) {
            redisCacheUserService.findById(id);
        }

        //set a breakpoint here to see what is in cache
        log.info("Deleting existing users");
        for (Long id : ids) {
            redisCacheUserService.deleteUser(id);
        }

        log.info("cache and db cleared");
        //set a breakpoint here to see what is in cache and DB
    }
}
