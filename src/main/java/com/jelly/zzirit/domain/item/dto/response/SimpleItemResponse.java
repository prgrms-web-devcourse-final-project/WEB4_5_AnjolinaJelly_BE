package com.jelly.zzirit.domain.item.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;

public record SimpleItemResponse(
	Long itemId,
	String name,
	String type,
	String brand,
	String imageUrl,
	BigDecimal originalPrice,
	BigDecimal discountedPrice,
	ItemStatus timeDealStatus,
	Integer discountRatio,
	LocalDateTime endTime
) {

	public static SimpleItemResponse from(TimeDealItem timeDealItem) {
		return new SimpleItemResponse(
			timeDealItem.getItem().getId(),
			timeDealItem.getItem().getName(),
			timeDealItem.getItem().getTypeBrand().getType().getName(),
			timeDealItem.getItem().getTypeBrand().getBrand().getName(),
			timeDealItem.getItem().getImageUrl(),
			timeDealItem.getItem().getPrice(),
			timeDealItem.getPrice(),
			timeDealItem.getItem().getItemStatus(),
			timeDealItem.getTimeDeal().getDiscountRatio(),
			timeDealItem.getTimeDeal().getEndTime()
		);
	}

	public static SimpleItemResponse from(Item item) {
		return new SimpleItemResponse(
			item.getId(),
			item.getName(),
			item.getTypeBrand().getType().getName(),
			item.getTypeBrand().getBrand().getName(),
			item.getImageUrl(),
			item.getPrice(),
			BigDecimal.ZERO,
			item.getItemStatus(),
			0,
			null
		);
	}
}
