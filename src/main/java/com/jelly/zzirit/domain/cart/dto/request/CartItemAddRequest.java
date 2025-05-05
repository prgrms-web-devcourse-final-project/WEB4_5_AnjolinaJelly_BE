package com.jelly.zzirit.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartItemAddRequest {
	@NotNull
	private Long itemId;

	@NotNull
	@Min(1)
	private Integer quantity;

}