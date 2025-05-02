package com.jelly.zzirit.domain.order.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.jelly.zzirit.global.config.AppConfig;

public record TossPaymentRequest(
	BigDecimal amount,
	String orderId,
	String orderName,
	String successUrl,
	String failUrl
) {

	public static TossPaymentRequest of(PaymentRequestDto dto, String orderId) {
		return new TossPaymentRequest(
			dto.totalAmount(),
			orderId,
			makeOrderName(dto.orderItems()),
			AppConfig.getTossSuccessUrl(),
			AppConfig.getTossFailUrl()
		);
	}

	private static String makeOrderName(List<OrderItemRequestDto> items) {
		if (items.size() == 1) {
			return items.getFirst().itemName();
		}
		return items.getFirst().itemName() + " 외 " + (items.size() - 1) + "건";
	}
}