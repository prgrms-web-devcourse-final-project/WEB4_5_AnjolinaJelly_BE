package com.jelly.zzirit.domain.item.dto.timeDeal.response;

import java.time.LocalDateTime;
import java.util.List;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.timeDeal.dto.response.SearchTimeDealItem;

public record SearchTimeDeal(
	Long timeDealId,
	String title,
	LocalDateTime startTime,
	LocalDateTime endTime,
	TimeDeal.TimeDealStatus status,
	Integer discountRatio,
	List<SearchTimeDealItem> items
) {
}
