package backend.academy.caffeine.memoization;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.math.BigInteger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FactorialMemoization {
    private static final Cache<Integer, BigInteger> cache = Caffeine.newBuilder()
        .maximumSize(1000)
        .recordStats()
        .build();

    public static void main(String[] args) {
        FactorialMemoization factorialMemoization = new FactorialMemoization();
        log.info("factorial of 1000 is {}", factorialMemoization.factorial(1000));
        log.info("factorial of 800 is {}", factorialMemoization.factorial(800));
        log.info(cache.stats().toString());
    }

    public BigInteger factorial(int n) {
        if (n == 1) {
            return BigInteger.ONE;
        }
        BigInteger factorial = cache.getIfPresent(n);
        if (factorial != null) {
            return factorial;
        }
        BigInteger multiply = factorial(n - 1).multiply(BigInteger.valueOf(n));
        cache.put(n, multiply);
        return multiply;
    }
}
