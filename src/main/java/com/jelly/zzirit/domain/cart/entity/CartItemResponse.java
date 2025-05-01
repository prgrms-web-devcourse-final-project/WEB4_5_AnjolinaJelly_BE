package com.jelly.zzirit.domain.cart.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
	private Long cartItemId;

	private Long itemId;
	private String itemName;
	private String itemImageUrl;

	private int quantity;
	private int unitPrice; // 구매 단가
	private int totalPrice;

	private boolean isTimeDeal; // true = 타임딜 상품
	private Integer discountRatio; // 타임딜일 경우만 노출
}