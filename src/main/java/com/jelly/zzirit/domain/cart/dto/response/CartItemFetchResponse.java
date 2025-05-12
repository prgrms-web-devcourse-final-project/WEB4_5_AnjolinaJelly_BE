package com.jelly.zzirit.domain.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장바구니 상품 응답 DTO")
public record CartItemFetchResponse(
	Long cartItemId,
	Long itemId,
	String itemName,
	String type,
	String brand,
	Integer quantity,
	String imageUrl,
	Integer originalPrice,
	Integer discountedPrice,
	Integer totalPrice,
	Boolean isTimeDeal,
	Integer discountRatio,
	Boolean isSoldOut
) {
}