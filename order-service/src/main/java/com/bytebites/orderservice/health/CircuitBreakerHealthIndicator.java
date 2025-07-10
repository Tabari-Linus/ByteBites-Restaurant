package com.bytebites.orderservice.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class CircuitBreakerHealthIndicator implements HealthIndicator {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerHealthIndicator(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public Health health() {
        Health.Builder builder = Health.up();

        Map<String, String> circuitBreakerStates = StreamSupport.stream(circuitBreakerRegistry.getAllCircuitBreakers().spliterator(), false)
                .collect(Collectors.toMap(
                        CircuitBreaker::getName,
                        circuitBreaker -> circuitBreaker.getState().toString()
                ));

        boolean hasOpenCircuits = circuitBreakerStates.values().stream()
                .anyMatch(state -> "OPEN".equals(state));

        if (hasOpenCircuits) {
            builder.down();
        }

        builder.withDetail("circuitBreakers", circuitBreakerStates);

        StreamSupport.stream(circuitBreakerRegistry.getAllCircuitBreakers().spliterator(), false).forEach(cb -> {
            CircuitBreaker.Metrics metrics = cb.getMetrics();
            builder.withDetail(cb.getName() + "Metrics", Map.of(
                    "failureRate", metrics.getFailureRate(),
                    "numberOfCalls", metrics.getNumberOfBufferedCalls(),
                    "numberOfFailedCalls", metrics.getNumberOfFailedCalls(),
                    "numberOfSuccessfulCalls", metrics.getNumberOfSuccessfulCalls()
            ));
        });

        return builder.build();
    }
}