package com.jelly.zzirit.domain.cart.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
	private Long cartId;
	private List<CartItemResponse> items;
	private int totalQuantity;
	private int totalAmount;
}