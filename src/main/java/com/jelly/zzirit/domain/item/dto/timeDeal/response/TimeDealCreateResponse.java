package com.jelly.zzirit.domain.item.dto.timeDeal.response;

import java.util.List;

public record TimeDealCreateResponse(
	Long timeDealId,
	String title,
	String startTime,
	String endTime,
	Integer discountRate,
	List<TimeDealCreateItem> items
) {
	public record TimeDealCreateItem(
		Long itemId,
		Integer quantity
	) {
	}
}
