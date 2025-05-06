package com.jelly.zzirit.domain.item.dto.response;

import java.math.BigDecimal;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;

public record SimpleItemResponse(
	Long itemId,
	String name,
	String type,
	String brand,
	BigDecimal price,
	ItemStatus timeDealStatus
) {

	public static SimpleItemResponse from(TimeDealItem timeDealItem) {
		return new SimpleItemResponse(
			timeDealItem.getItem().getId(),
			timeDealItem.getItem().getName(),
			timeDealItem.getItem().getTypeBrand().getType().getName(),
			timeDealItem.getItem().getTypeBrand().getBrand().getName(),
			timeDealItem.getPrice(),
			timeDealItem.getItem().getItemStatus()
		);
	}

	public static SimpleItemResponse from(Item item) {
		return new SimpleItemResponse(
			item.getId(),
			item.getName(),
			item.getTypeBrand().getType().getName(),
			item.getTypeBrand().getBrand().getName(),
			item.getPrice(),
			item.getItemStatus()
		);
	}
}
