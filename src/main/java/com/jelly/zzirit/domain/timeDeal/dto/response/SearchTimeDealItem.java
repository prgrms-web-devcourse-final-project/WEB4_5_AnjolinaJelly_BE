package com.jelly.zzirit.domain.timeDeal.dto.response;

import java.math.BigDecimal;

public record SearchTimeDealItem(
	Long itemId,
	String itemName,
	int quantity,
	BigDecimal originalPrice,
	BigDecimal finalPrice
) {
}
