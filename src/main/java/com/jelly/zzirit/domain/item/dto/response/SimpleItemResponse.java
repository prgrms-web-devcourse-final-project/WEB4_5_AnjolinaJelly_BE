package com.jelly.zzirit.domain.item.dto.response;

import com.jelly.zzirit.domain.item.entity.ItemStatus;

public record SimpleItemResponse(
	Long itemId,
	String name,
	String type,
	String brand,
	Integer price,
	ItemStatus timeDealStatus
) {

}
