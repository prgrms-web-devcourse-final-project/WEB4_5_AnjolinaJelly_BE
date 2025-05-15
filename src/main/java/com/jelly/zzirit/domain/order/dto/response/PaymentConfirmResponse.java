package com.jelly.zzirit.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 확인 응답 DTO")
public record PaymentConfirmResponse(

	@Schema(description = "주문번호", example = "ORD123456789")
	String orderId,

	@Schema(description = "결제 키", example = "pay_abc123xyz456")
	String paymentKey,

	@Schema(description = "결제 금액", example = "12500")
	int amount
) {}