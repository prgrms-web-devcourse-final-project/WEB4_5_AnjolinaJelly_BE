package com.jelly.zzirit.domain.cart.dto.request;

import lombok.Getter;

@Getter
public class CartItemAddRequest {
	private Long itemId;
	private int quantity;
	private boolean isTimeDeal;
}