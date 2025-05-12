package com.jelly.zzirit.domain.item.dto.response;

import java.util.List;

public record TimeDealCreateResponse(
	Long timeDealId,
	String timeDealName,
	String startTime,
	String endTime,
	Integer discountRatio,
	List<TimeDealCreateItem> items
) {
	public static TimeDealCreateResponse from(Long timeDealId, String timeDealName, String startTime, String endTime,
		Integer discountRatio, List<TimeDealCreateItem> items) {
		return new TimeDealCreateResponse(timeDealId, timeDealName, startTime, endTime, discountRatio, items);
	}

	public record TimeDealCreateItem(
		Long itemId,
		Integer quantity
	) {
		public static TimeDealCreateItem from(Long itemId, Integer quantity) {
			return new TimeDealCreateItem(itemId, quantity);
		}
	}
}
