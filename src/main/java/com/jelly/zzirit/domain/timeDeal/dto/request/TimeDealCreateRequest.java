package com.jelly.zzirit.domain.timeDeal.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.jelly.zzirit.domain.item.dto.timeDeal.request.TimeDealCreateItemDetail;

public record TimeDealCreateRequest(
	String title,
	LocalDateTime startTime,
	LocalDateTime endTime,
	int discountRate,
	List<TimeDealCreateItemDetail> items
) {
}