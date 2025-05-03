package com.jelly.zzirit.domain.order.dto.request;

import java.math.BigDecimal;

public record OrderItemRequestDto(
	Long itemId,
	Long timeDealItemId,
	int quantity,
	String itemName,
	BigDecimal price
) {
	public boolean isTimeDeal() {
		return timeDealItemId != null;
	}

	public Long getStockTargetId() {
		return isTimeDeal() ? timeDealItemId : itemId;
	}
}