package ru.tbank.sem3.context;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;

import java.util.concurrent.atomic.AtomicInteger;

@EqualsAndHashCode
@Slf4j
public class ContextCounter implements ContextCustomizer {

    private static final AtomicInteger CONTEXT_COUNTER = new AtomicInteger();

    @Override
    public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        int numberOfContexts = CONTEXT_COUNTER.incrementAndGet();
        log.info("Creating new context. Current context count: {}", numberOfContexts);
    }
}
