package com.jelly.zzirit.domain.item.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;

public record TimeDealFetchResponse(
	Long timeDealId,
	String timeDealName,
	LocalDateTime startTime,
	LocalDateTime endTime,
	TimeDeal.TimeDealStatus status,
	Integer discountRatio,
	List<TimeDealFetchItem> items
) {
	public static TimeDealFetchResponse from(TimeDeal deal, List<TimeDealFetchItem> items) {
		return new TimeDealFetchResponse(
			deal.getId(),
			deal.getName(),
			deal.getStartTime(),
			deal.getEndTime(),
			deal.getStatus(),
			deal.getDiscountRatio(),
			items
		);
	}

	public record TimeDealFetchItem(
		Long itemId,
		String itemName,
		int quantity,
		BigDecimal originalPrice,
		BigDecimal discountedPrice
	) {
		public static TimeDealFetchItem from(Long itemId, String itemName, int quantity,
			BigDecimal originalPrice, BigDecimal discountedPrice) {
			return new TimeDealFetchItem(itemId, itemName, quantity, originalPrice, discountedPrice);
		}
	}
}
