package com.jelly.zzirit.domain.item.dto.request;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal.TimeDealStatus;

public record TimeDealSearchCondition(
	String timeDealName,
	Long timeDealId,
	String timeDealItemName,
	Long timeDealItemId,
	TimeDealStatus status
) {
	public static TimeDealSearchCondition from(String timeDealName, Long timeDealId, String timeDealItemName,
		Long timeDealItemId, TimeDealStatus status) {
		return new TimeDealSearchCondition(timeDealName, timeDealId, timeDealItemName, timeDealItemId, status);
	}
}