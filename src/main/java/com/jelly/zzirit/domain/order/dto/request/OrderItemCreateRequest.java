package com.jelly.zzirit.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "주문 항목 요청 DTO")
public record OrderItemCreateRequest(

	@Schema(description = "상품 ID", example = "1")
	@NotNull(message = "상품 ID는 필수입니다.")
	Long itemId,

	@Schema(description = "상품 이름", example = "모나미 볼펜")
	@NotBlank(message = "상품 이름은 필수입니다.")
	String itemName,

	@Schema(description = "주문 수량", example = "2")
	@Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
	int quantity

) {}