package com.jelly.zzirit.domain.item.dto.response.timeDeal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;

public record TimeDealSearchResponse(
	Long timeDealId,
	String timeDealName,
	LocalDateTime startTime,
	LocalDateTime endTime,
	TimeDeal.TimeDealStatus status,
	Integer discountRatio,
	List<TimeDealSearchItem> items
) {
	public static TimeDealSearchResponse from(TimeDeal deal, List<TimeDealSearchItem> items) {
		return new TimeDealSearchResponse(
			deal.getId(),
			deal.getName(),
			deal.getStartTime(),
			deal.getEndTime(),
			deal.getStatus(),
			deal.getDiscountRatio(),
			items
		);
	}

	public record TimeDealSearchItem(
		Long itemId,
		String itemName,
		int quantity,
		BigDecimal originalPrice,
		BigDecimal discountedPrice
	) {
		public static TimeDealSearchItem from(Long itemId, String itemName, int quantity,
			BigDecimal originalPrice, BigDecimal discountedPrice) {
			return new TimeDealSearchItem(itemId, itemName, quantity, originalPrice, discountedPrice);
		}
	}
}
