package com.jelly.zzirit.domain.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장바구니 상품 추가 요청 DTO")
public record CartItemCreateRequest(
	Long itemId,
	Integer quantity
) {
}