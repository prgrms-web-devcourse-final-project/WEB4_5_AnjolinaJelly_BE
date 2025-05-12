package com.jelly.zzirit.domain.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "장바구니 상품 추가 요청 DTO")
public record CartItemCreateRequest(
	@NotNull Long itemId,
	@NotNull @Min(1) Integer quantity
) {
}