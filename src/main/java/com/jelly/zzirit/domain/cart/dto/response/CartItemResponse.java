package com.jelly.zzirit.domain.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartItemResponse {
	private Long cartItemId;
	private Long itemId;
	private String itemName;
	private String itemImageUrl;
	private int quantity;
	private int unitPrice;

	//private int discountedPrice; // 할인된 가격

	private int totalPrice;
	private boolean isTimeDeal;
	private Integer discountRatio;
}
