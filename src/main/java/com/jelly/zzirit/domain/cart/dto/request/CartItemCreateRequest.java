package com.jelly.zzirit.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartItemCreateRequest {
	@NotNull
	private Long itemId;

	@NotNull
	@Min(value = 1, message = "최솟값은 1개 이상이여야 합니다")
	private Integer quantity;
}