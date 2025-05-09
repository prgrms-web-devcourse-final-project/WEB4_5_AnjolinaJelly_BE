package com.jelly.zzirit.domain.order.dto.request;

import java.math.BigDecimal;

public record OrderItemRequestDto(
	Long itemId,
	int quantity,
	String itemName,
	BigDecimal price
) {}