package com.jelly.zzirit.domain.order.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

	private String orderId;
	private String paymentKey;
	private String method;
	private String status;
	private BigDecimal totalAmount;
}