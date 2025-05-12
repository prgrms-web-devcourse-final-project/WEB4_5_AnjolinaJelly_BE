package com.jelly.zzirit.domain.order.dto.request;

import java.math.BigDecimal;

public record OrderItemCreateRequest(
	Long itemId,
	Integer quantity,
	String itemName,
	BigDecimal price
) {}