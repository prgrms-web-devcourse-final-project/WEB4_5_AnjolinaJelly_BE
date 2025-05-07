package com.jelly.zzirit.domain.cart.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "장바구니 응답 DTO")
public class CartResponse {

	@Schema(description = "장바구니 ID", example = "1001")
	private Long cartId;

	@Schema(description = "장바구니 상품 목록")
	private List<CartItemResponse> items;

	@Schema(description = "장바구니 전체 수량", example = "5")
	private int cartTotalQuantity;

	@Schema(description = "장바구니 총 가격", example = "5000000")
	private int cartTotalPrice;
}