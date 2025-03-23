package backend.academy.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.index.qual.NonNegative;


@Slf4j
public class Example {

    private static final LoadingCache<String, Integer> cache = Caffeine.newBuilder()
        .maximumSize(100)
        .recordStats()
        .expireAfterWrite(Duration.ofSeconds(100))
        .removalListener((k, v, cause) -> System.out.println("Removed " + k + " => " + v + " due to " + cause))
        .build(Example::getValue);

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            cache.put("" + i, i);
        }
        log.info("getting value for key=3. value={}", cache.get("3"));
        log.info("getting value for key=99. value={}", cache.get("99"));
        log.info("getting value for key=100. value={}", cache.get("100"));
        log.info("getting value for key=100. value={}", cache.get("100"));
        log.info("getting value for key='Not a number'. value={}", cache.get("Not a number"));
        log.info("getting value for key='Not a number'. value={}", cache.get("Not a number"));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info(cache.stats().toString());
    }

    private static Integer getValue(String key) {
        log.info("Retrieving value for key:{} using CacheLoader", key);
        try {
            return Integer.parseInt(key);
        } catch (NumberFormatException ex) {
            log.info("Key is not a number. Falling back to default value=0");
        }
        return 0;
    }

}
