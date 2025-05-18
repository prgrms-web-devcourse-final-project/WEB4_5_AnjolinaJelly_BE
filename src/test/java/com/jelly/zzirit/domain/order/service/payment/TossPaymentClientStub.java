package com.jelly.zzirit.domain.order.service.payment;

import java.math.BigDecimal;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;

import groovy.util.logging.Slf4j;

@Slf4j
@Component
@Profile("test")
public class TossPaymentClientStub extends TossPaymentClient {

	public TossPaymentClientStub() {
		super(null, null);
	}

	@Override
	public void confirmPayment(String paymentKey, String orderId, String amount) {
	}

	@Override
	public PaymentResponse fetchPaymentInfo(String paymentKey) {
		return new PaymentResponse(
			"ORD123",
			paymentKey,
			"카드",
			"DONE",
			new BigDecimal("15000")
		);
	}

	@Override
	public void refund(String paymentKey, BigDecimal amount, String reason) {
	}
}
