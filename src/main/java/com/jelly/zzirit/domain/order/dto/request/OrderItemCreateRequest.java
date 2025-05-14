package com.jelly.zzirit.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 항목 요청 DTO")
public record OrderItemCreateRequest(

	@Schema(description = "상품 ID", example = "1", required = true)
	Long itemId,

	@Schema(description = "상품 이름 (프론트에서 결제창 표시용)", example = "모나미 볼펜", required = true)
	String itemName,

	@Schema(description = "주문 수량", example = "2", required = true)
	int quantity

) {}