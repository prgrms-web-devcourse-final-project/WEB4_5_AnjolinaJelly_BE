package com.jelly.zzirit.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CartItemAddRequest {
	@NotNull
	private Long itemId;

	@Min(1)
	private int quantity;

	private boolean isTimeDeal;
}