package com.jelly.zzirit.domain.item.dto.response.timeDeal;

import java.util.List;

public record TimeDealCreateResponse(
	Long timeDealId,
	String timeDealName,
	String startTime,
	String endTime,
	Integer discountRatio,
	List<TimeDealCreateItem> items
) {
	public record TimeDealCreateItem(
		Long itemId,
		Integer quantity
	) {
	}
}
