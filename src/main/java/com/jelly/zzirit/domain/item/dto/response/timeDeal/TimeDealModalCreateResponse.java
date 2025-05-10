package com.jelly.zzirit.domain.item.dto.response.timeDeal;

public record TimeDealModalCreateResponse(
	Long itemId,
	String itemName,
	Integer originalPrice
) {
}