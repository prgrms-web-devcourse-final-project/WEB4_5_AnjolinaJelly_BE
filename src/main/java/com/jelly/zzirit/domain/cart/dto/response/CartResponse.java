package com.jelly.zzirit.domain.cart.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartResponse {
	private Long cartId;
	private List<CartItemResponse> items;
	private int totalQuantity;
	private int totalAmount;
}