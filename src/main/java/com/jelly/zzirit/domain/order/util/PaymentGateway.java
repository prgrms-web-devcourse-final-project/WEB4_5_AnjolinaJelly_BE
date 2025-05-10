package com.jelly.zzirit.domain.order.util;

import java.math.BigDecimal;

import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;

public interface PaymentGateway {

	void confirmPayment(String paymentKey, String orderId, String amount);

	PaymentResponse fetchPaymentInfo(String paymentKey);

	void refund(String paymentKey, BigDecimal amount, String reason);
}