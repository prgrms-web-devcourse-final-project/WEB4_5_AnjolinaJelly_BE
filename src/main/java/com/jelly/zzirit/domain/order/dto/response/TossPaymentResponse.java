package com.jelly.zzirit.domain.order.dto.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossPaymentResponse {
	private String orderId;
	private String paymentKey;
	private String method;
	private String status;
	private BigDecimal totalAmount;
}