package com.jelly.zzirit.domain.order.domain.fixture;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.entity.PaymentMethod;

import java.util.UUID;

public class PaymentFixture {
    public static Payment 결제_정보_생성(Order order) {
        return Payment.builder()
            .paymentKey(generateUniquePaymentKey())
            .order(order)
            .paymentMethod(PaymentMethod.CARD)
            .paymentStatus(Payment.PaymentStatus.DONE)
            .build();
    }

    private static String generateUniquePaymentKey() {
        return "payment-key-" + UUID.randomUUID();
    }
}
