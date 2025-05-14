package com.jelly.zzirit.domain.order.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 요청 DTO")
public record PaymentRequest(

	@ArraySchema(
		schema = @Schema(implementation = OrderItemCreateRequest.class),
		arraySchema = @Schema(description = "주문 항목 리스트", required = true)
	)
	List<OrderItemCreateRequest> orderItems,

	@Schema(description = "총 결제 금액 (단위: 원)", example = "15000", required = true)
	int totalAmount,

	@Schema(description = "배송 요청사항", example = "문 앞에 놓아주세요")
	String shippingRequest,

	@Schema(description = "기본 주소", example = "서울시 종로구 삼일대로 123", required = true)
	String address,

	@Schema(description = "상세 주소", example = "101동 202호")
	String addressDetail,

	@Schema(description = "결제 성공 시 리디렉션될 URL", example = "https://zzirit.vercel.app/payment/success", required = true)
	String successUrl,

	@Schema(description = "결제 실패 시 리디렉션될 URL", example = "https://zzirit.vercel.app/payment/fail", required = true)
	String failUrl

) {}