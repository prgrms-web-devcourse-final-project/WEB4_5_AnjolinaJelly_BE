package com.jelly.zzirit.domain.item.dto.request;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;

public record TimeDealSearchCondition(
	String timeDealName,
	Long timeDealId,
	String timeDealItemName,
	Long timeDealItemId,
	TimeDeal.TimeDealStatus status
) {
	public static TimeDealSearchCondition from(String timeDealName, Long timeDealId, String timeDealItemName,
		Long timeDealItemId, TimeDeal.TimeDealStatus status) {
		return new TimeDealSearchCondition(timeDealName, timeDealId, timeDealItemName, timeDealItemId, status);
	}
}