package com.jelly.zzirit.domain.item.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;

public record SimpleItemFetchResponse(
	Long itemId,
	String name,
	String type,
	String brand,
	String imageUrl,
	BigDecimal originalPrice,
	BigDecimal discountedPrice,
	ItemStatus itemStatus,
	Integer discountRatio,
	LocalDateTime endTimeDeal
) {

	public static SimpleItemFetchResponse from(ItemFetchQueryResponse items) {
		return new SimpleItemFetchResponse(
			items.itemId(),
			items.name(),
			items.type(),
			items.brand(),
			items.imageUrl(),
			items.originalPrice(),
			items.discountedPrice(),
			items.itemStatus(),
			items.discountRatio(),
			items.endTimeDeal()
		);
	}
}
