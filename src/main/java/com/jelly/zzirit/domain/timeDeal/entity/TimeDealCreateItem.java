package com.jelly.zzirit.domain.timeDeal.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "타임딜 등록 상품 항목")
public class TimeDealCreateItem {
	@Schema(description = "상품 ID", example = "1")
	public Long itemId;

	@Schema(description = "상품 수량", example = "5")
	public Integer quantity;

	public TimeDealCreateItem(Long itemId, Integer quantity) {
		this.itemId = itemId;
		this.quantity = quantity;
	}
}
