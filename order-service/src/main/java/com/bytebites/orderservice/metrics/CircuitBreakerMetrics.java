package com.bytebites.orderservice.metrics;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.stream.StreamSupport;

@Component
public class CircuitBreakerMetrics {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final MeterRegistry meterRegistry;

    public CircuitBreakerMetrics(CircuitBreakerRegistry circuitBreakerRegistry,
                                 MeterRegistry meterRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void bindMetrics() {
        StreamSupport.stream(circuitBreakerRegistry.getAllCircuitBreakers().spliterator(), false)
                .forEach(this::bindCircuitBreakerMetrics);
    }

    private void bindCircuitBreakerMetrics(CircuitBreaker circuitBreaker) {
        String name = circuitBreaker.getName();

        Gauge.builder("circuit_breaker_state", circuitBreaker, cb -> {
                    switch (cb.getState()) {
                        case CircuitBreaker.State.CLOSED: return 0;
                        case CircuitBreaker.State.OPEN: return 1;
                        case CircuitBreaker.State.HALF_OPEN: return 2;
                        default: return -1;
                    }
                })
                .description("Circuit breaker state")
                .tag("name", name)
                .register(meterRegistry);

        Gauge.builder("circuit_breaker_failure_rate", circuitBreaker, cb -> cb.getMetrics().getFailureRate())
                .description("Circuit breaker failure rate")
                .tag("name", name)
                .register(meterRegistry);

        Gauge.builder("circuit_breaker_buffered_calls", circuitBreaker, cb -> cb.getMetrics().getNumberOfBufferedCalls())
                .description("Circuit breaker buffered calls")
                .tag("name", name)
                .register(meterRegistry);
    }
}