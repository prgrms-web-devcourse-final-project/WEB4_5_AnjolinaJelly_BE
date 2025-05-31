package com.jelly.zzirit.domain.order.util;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CircuitBreakerStatusChecker {

    private final CircuitBreakerRegistry registry;

    public boolean isCircuitOpen(String name) {
        return registry.circuitBreaker(name).getState() == CircuitBreaker.State.OPEN;
    }

    public boolean isCircuitHalfOpen(String name) {
        return registry.circuitBreaker(name).getState() == CircuitBreaker.State.HALF_OPEN;
    }
}