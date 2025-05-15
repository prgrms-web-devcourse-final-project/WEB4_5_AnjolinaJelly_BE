package com.jelly.zzirit.domain.item.dto.response;

import java.util.List;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;

public record TimeDealCreateResponse(
	Long timeDealId,
	String timeDealName,
	String startTime,
	String endTime,
	Integer discountRatio,
	List<TimeDealCreateItem> items
) {
	public static TimeDealCreateResponse from(TimeDeal timeDeal, List<TimeDealCreateItem> items) {
		return new TimeDealCreateResponse(
			timeDeal.getId(),
			timeDeal.getName(),
			timeDeal.getStartTime().toString(),
			timeDeal.getEndTime().toString(),
			timeDeal.getDiscountRatio(),
			items
		);
	}

	public record TimeDealCreateItem(
		Long itemId,
		Integer quantity
	) {
		public static TimeDealCreateItem from(Long itemId, Integer quantity) {
			return new TimeDealCreateItem(itemId, quantity);
		}

		public static TimeDealCreateItem from(TimeDealItem timeDealItem, int quantity) {
			return new TimeDealCreateItem(timeDealItem.getItem().getId(), quantity);
		}
	}
}
