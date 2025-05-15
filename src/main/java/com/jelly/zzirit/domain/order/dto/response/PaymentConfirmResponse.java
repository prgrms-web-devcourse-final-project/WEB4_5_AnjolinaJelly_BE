package com.jelly.zzirit.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentConfirmResponse {

	@Schema(description = "주문번호", example = "ORD123456789")
	private String orderId;

	@Schema(description = "결제 키", example = "pay_abc123xyz456")
	private String paymentKey;

	@Schema(description = "결제 금액", example = "12500")
	private int amount;
}