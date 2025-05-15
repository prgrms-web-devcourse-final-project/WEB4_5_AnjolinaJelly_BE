package com.jelly.zzirit.domain.order.dto.request;

public record TossPaymentWebhookRequest(
	String eventType,
	String createdAt,
	PaymentData data
) {
	public record PaymentData(
		String paymentKey,
		String orderId,
		String status
	) {}
}