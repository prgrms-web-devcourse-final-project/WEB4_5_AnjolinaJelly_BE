package com.jelly.zzirit.domain.item.dto.response.timeDeal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.jelly.zzirit.domain.item.entity.Brand;
import com.jelly.zzirit.domain.item.entity.Type;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;

public record CurrentTimeDealResponse(
	Long timeDealId,
	String timeDealName,
	LocalDateTime startTime,
	LocalDateTime endTime,
	Integer discountRatio,
	TimeDeal.TimeDealStatus status,
	List<CurrentTimeDealItem> items
) {
	public record CurrentTimeDealItem(
		Long itemId,
		String imageUrl,
		BigDecimal originalPrice,
		BigDecimal discountedPrice,
		Type type,
		Brand brand
	) {
	}
}