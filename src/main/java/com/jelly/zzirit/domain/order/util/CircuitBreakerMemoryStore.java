package com.jelly.zzirit.domain.order.util;


import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class CircuitBreakerMemoryStore {

    private static final AtomicReference<String> failedPaymentKey = new AtomicReference<>();

    private CircuitBreakerMemoryStore() {}

    public static void saveFailedPaymentKey(String paymentKey) {
        failedPaymentKey.set(paymentKey);
    }

    public static Optional<String> getStoredKey() {
        return Optional.ofNullable(failedPaymentKey.get());
    }

    public static void remove() {
        failedPaymentKey.set(null);
    }
}