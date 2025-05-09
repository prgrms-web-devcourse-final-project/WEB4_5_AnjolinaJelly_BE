package com.jelly.zzirit.domain.item.dto.request;

import java.time.LocalDateTime;
import java.util.List;

public record TimeDealCreateRequest(
	String timeDealName,
	LocalDateTime startTime,
	LocalDateTime endTime,
	int discountRatio,
	List<TimeDealCreateItemDetail> items
) {
	public record TimeDealCreateItemDetail(
		Long itemId,
		int quantity
	) {
	}
}