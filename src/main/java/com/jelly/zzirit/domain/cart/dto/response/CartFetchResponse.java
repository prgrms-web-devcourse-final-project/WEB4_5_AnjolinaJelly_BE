package com.jelly.zzirit.domain.cart.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "장바구니 응답 DTO")
public record CartFetchResponse(
	@NotNull Long cartId,
	List<CartItemFetchResponse> items,
	Integer cartTotalQuantity,
	Integer cartTotalPrice
) {
}