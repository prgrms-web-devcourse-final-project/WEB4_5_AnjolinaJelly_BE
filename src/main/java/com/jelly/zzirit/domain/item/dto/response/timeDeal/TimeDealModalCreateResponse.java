package com.jelly.zzirit.domain.item.dto.response.timeDeal;

import com.jelly.zzirit.domain.item.entity.Item;

public record TimeDealModalCreateResponse(
	Long itemId,
	String itemName,
	Integer originalPrice
) {
	public static TimeDealModalCreateResponse from(Item item) {
		return new TimeDealModalCreateResponse(
			item.getId(),
			item.getName(),
			item.getPrice().intValue()
		);
	}
}