package com.jelly.zzirit.domain.item.dto.response;

import java.time.LocalDateTime;

import com.jelly.zzirit.domain.item.entity.TimeDealStatus;

public record ItemResponse(
	Long itemId,
	String name,
	String type,
	String brand,
	Integer quantity,
	Integer price,
	TimeDealStatus timeDealStatus,
	LocalDateTime endTimeDeal
) {
}
