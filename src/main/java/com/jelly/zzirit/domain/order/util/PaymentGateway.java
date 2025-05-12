package com.jelly.zzirit.domain.order.util;

import java.math.BigDecimal;

import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;
import com.jelly.zzirit.domain.order.entity.Order;

public interface PaymentGateway {

	void confirmPayment(String paymentKey, String orderId, String amount);

	PaymentResponse fetchPaymentInfo(String paymentKey);

	void refund(String paymentKey, BigDecimal amount, String reason);

	PaymentProvider  getPaymentProvider();

	void validate(Order order, PaymentResponse response, String amount);

}