package com.jelly.zzirit.domain.timeDeal.dto.response;

import java.math.BigDecimal;

import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.Type;

public record CurruntTimeDealItem(
	Long itemId,
	String imageUrl,
	BigDecimal originalPrice,
	BigDecimal finalPrice,
	Type type,
	Brand brand
) {
}
