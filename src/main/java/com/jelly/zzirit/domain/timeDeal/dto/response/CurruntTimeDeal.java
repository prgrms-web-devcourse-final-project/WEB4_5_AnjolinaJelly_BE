package com.jelly.zzirit.domain.timeDeal.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;

public record CurruntTimeDeal(
	Long timeDealId,
	String title,
	LocalDateTime startTime,
	LocalDateTime endTime,
	Integer discountRatio,
	TimeDeal.TimeDealStatus status,
	List<CurruntTimeDealItem> items
) {
}