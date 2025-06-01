package com.jelly.zzirit.domain.item.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.querydsl.core.annotations.QueryProjection;

public record ItemFetchQueryResponse(
	Long itemId,
	String name,
	String type,
	String brand,
	String imageUrl,
	BigDecimal originalPrice,
	BigDecimal discountedPrice,
	ItemStatus itemStatus,
	Integer discountRatio,
	LocalDateTime endTimeDeal
) {

	@QueryProjection
	public ItemFetchQueryResponse {
	}
}
