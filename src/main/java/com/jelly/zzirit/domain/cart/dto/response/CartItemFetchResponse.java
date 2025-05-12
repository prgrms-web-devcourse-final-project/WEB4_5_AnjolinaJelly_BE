package com.jelly.zzirit.domain.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "장바구니 상품 응답 DTO")
public class CartItemFetchResponse {
	private Long cartItemId;
	private Long itemId;
	private String itemName;
	private String type;
	private String brand;
	private Integer quantity;
	private String imageUrl;
	private Integer originalPrice;
	private Integer discountedPrice;
	private Integer totalPrice;
	private Boolean isTimeDeal;
	private Integer discountRatio;
	private Boolean isSoldOut;
}