package com.jelly.zzirit.domain.item.dto.response.timeDeal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;

public record TimeDealSearchResponse(
	Long timeDealId,
	String title,
	LocalDateTime startTime,
	LocalDateTime endTime,
	TimeDeal.TimeDealStatus status,
	Integer discountRatio,
	List<TimeDealSearchItem> items
) {
	public record TimeDealSearchItem(
		Long itemId,
		String itemName,
		int quantity,
		BigDecimal originalPrice,
		BigDecimal finalPrice
	) {
	}
}
