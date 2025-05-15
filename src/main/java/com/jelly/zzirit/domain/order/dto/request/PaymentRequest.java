package com.jelly.zzirit.domain.order.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

@Schema(description = "결제 요청 DTO")
public record PaymentRequest(

	@ArraySchema(
		schema = @Schema(implementation = OrderItemCreateRequest.class),
		arraySchema = @Schema(description = "주문 항목 리스트")
	)
	@NotEmpty(message = "주문 항목은 비어 있을 수 없습니다.")
	List<@Valid OrderItemCreateRequest> orderItems,

	@Schema(description = "총 결제 금액 (단위: 원)", example = "15000")
	@Positive(message = "결제 금액은 0보다 커야 합니다.")
	int totalAmount,

	@Schema(description = "배송 요청사항", example = "문 앞에 놔주세요")
	String shippingRequest,

	@Schema(description = "기본 주소", example = "서울시 종로구 종로1길 10")
	@NotBlank(message = "주소는 필수입니다.")
	String address,

	@Schema(description = "상세 주소", example = "101동 202호")
	String addressDetail

) {}