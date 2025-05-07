package com.jelly.zzirit.domain.item.dto.timeDeal.response;

public record TimeDealModalCreateResponse(
	Long itemId,
	String itemName,
	Integer originalPrice
) {
}