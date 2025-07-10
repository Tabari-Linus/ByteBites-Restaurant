package com.bytebites.orderservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CircuitBreakerEventListener {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerEventListener.class);

    @Bean
    public RegistryEventConsumer<CircuitBreaker> customCircuitBreakerEventListener() {
        return new RegistryEventConsumer<CircuitBreaker>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
                CircuitBreaker circuitBreaker = entryAddedEvent.getAddedEntry();
                logger.info("Circuit breaker '{}' added", circuitBreaker.getName());

                circuitBreaker.getEventPublisher()
                        .onStateTransition(event ->
                                logger.info("Circuit breaker '{}' state transition: {} -> {}",
                                        circuitBreaker.getName(),
                                        event.getStateTransition().getFromState(),
                                        event.getStateTransition().getToState()));

                circuitBreaker.getEventPublisher()
                        .onCallNotPermitted(event ->
                                logger.warn("Circuit breaker '{}' call not permitted", circuitBreaker.getName()));

                circuitBreaker.getEventPublisher()
                        .onFailureRateExceeded(event ->
                                logger.warn("Circuit breaker '{}' failure rate exceeded: {}%",
                                        circuitBreaker.getName(), event.getFailureRate()));
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {
                logger.info("Circuit breaker '{}' removed", entryRemoveEvent.getRemovedEntry().getName());
            }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {
                logger.info("Circuit breaker '{}' replaced", entryReplacedEvent.getOldEntry().getName());
            }

        };
    }
}