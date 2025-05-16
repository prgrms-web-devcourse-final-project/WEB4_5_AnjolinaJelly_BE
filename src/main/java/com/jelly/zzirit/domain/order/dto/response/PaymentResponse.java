package com.jelly.zzirit.domain.order.dto.response;

import java.math.BigDecimal;

public record PaymentResponse(
	String orderId,
	String paymentKey,
	String method,
	String status,
	BigDecimal totalAmount
) {}