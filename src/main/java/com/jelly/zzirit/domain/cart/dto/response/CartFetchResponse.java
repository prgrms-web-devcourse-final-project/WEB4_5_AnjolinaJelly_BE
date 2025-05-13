package com.jelly.zzirit.domain.cart.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "장바구니 응답 DTO")
public class CartFetchResponse {
	private Long cartId;
	private List<CartItemFetchResponse> items;
	private Integer cartTotalQuantity;
	private Integer cartTotalPrice;
}