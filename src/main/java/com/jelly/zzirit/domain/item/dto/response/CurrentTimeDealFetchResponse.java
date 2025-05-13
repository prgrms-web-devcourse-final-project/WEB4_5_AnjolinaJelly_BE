package com.jelly.zzirit.domain.item.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;

public record CurrentTimeDealFetchResponse(
	Long timeDealId,
	String timeDealName,
	LocalDateTime startTime,
	LocalDateTime endTime,
	Integer discountRatio,
	TimeDeal.TimeDealStatus status,
	List<CurrentTimeDealItem> items
) {
	public static CurrentTimeDealFetchResponse from(TimeDeal timeDeal, List<CurrentTimeDealItem> items) {
		return new CurrentTimeDealFetchResponse(
			timeDeal.getId(),
			timeDeal.getName(),
			timeDeal.getStartTime(),
			timeDeal.getEndTime(),
			timeDeal.getDiscountRatio(),
			timeDeal.getStatus(),
			items
		);
	}

	public static CurrentTimeDealFetchResponse from(
		Long id,
		String name,
		LocalDateTime startTime,
		LocalDateTime endTime,
		Integer discountRatio,
		TimeDeal.TimeDealStatus status,
		List<CurrentTimeDealItem> items
	) {
		return new CurrentTimeDealFetchResponse(id, name, startTime, endTime, discountRatio, status, items);
	}

	public record CurrentTimeDealItem(
		Long itemId,
		String imageUrl,
		BigDecimal originalPrice,
		BigDecimal discountedPrice,
		String type,
		String brand
	) {
		public static CurrentTimeDealItem from(Long itemId, String imageUrl, BigDecimal originalPrice,
			BigDecimal discountedPrice,
			String type, String brand) {
			return new CurrentTimeDealItem(itemId, imageUrl, originalPrice, discountedPrice, type, brand);
		}

		public static CurrentTimeDealItem from(com.jelly.zzirit.domain.item.entity.Item item,
			BigDecimal discountedPrice) {
			return new CurrentTimeDealItem(
				item.getId(),
				item.getImageUrl(),
				item.getPrice(),
				discountedPrice,
				item.getTypeBrand().getType().getName(),
				item.getTypeBrand().getBrand().getName()
			);
		}
	}
}