package com.jelly.zzirit.domain.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 초기화 응답 DTO")
public record PaymentInitResponse(

	@Schema(description = "주문 ID", example = "ORD20240514-000001")
	String orderId,

	@Schema(description = "결제 금액 (단위: 원)", example = "15000")
	int amount,

	@Schema(description = "주문명")
	String orderName,

	@Schema(description = "구매자 이름", example = "홍길동")
	String customerName
) {}