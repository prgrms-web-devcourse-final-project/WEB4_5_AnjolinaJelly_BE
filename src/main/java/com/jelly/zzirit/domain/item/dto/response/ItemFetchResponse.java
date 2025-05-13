package com.jelly.zzirit.domain.item.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;

public record ItemFetchResponse(
	Long itemId,
	String name,
	String type,
	String brand,
	Integer quantity,
	String imageUrl,
	BigDecimal originalPrice,
	BigDecimal discountedPrice,
	ItemStatus itemStatus,
	Integer discountRatio,
	LocalDateTime endTimeDeal
) {

	public static ItemFetchResponse from(TimeDealItem timeDealItem, Integer quantity) {
		return new ItemFetchResponse(
			timeDealItem.getItem().getId(),
			timeDealItem.getItem().getName(),
			timeDealItem.getItem().getTypeBrand().getType().getName(),
			timeDealItem.getItem().getTypeBrand().getBrand().getName(),
			quantity,
			timeDealItem.getItem().getImageUrl(),
			timeDealItem.getItem().getPrice(),
			timeDealItem.getPrice(),
			timeDealItem.getItem().getItemStatus(),
			timeDealItem.getTimeDeal().getDiscountRatio(),
			timeDealItem.getTimeDeal().getEndTime()
		);
	}

	public static ItemFetchResponse from(Item item, Integer quantity) {
		return new ItemFetchResponse(
			item.getId(),
			item.getName(),
			item.getTypeBrand().getType().getName(),
			item.getTypeBrand().getBrand().getName(),
			quantity,
			item.getImageUrl(),
			item.getPrice(),
			BigDecimal.ZERO,
			item.getItemStatus(),
			0,
			null
		);
	}
}
