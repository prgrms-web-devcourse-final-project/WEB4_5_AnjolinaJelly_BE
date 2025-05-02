package com.jelly.zzirit.domain.timeDeal.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "타임딜 생성 모달에서 사용하는 상품 정보")
public class TimeDealModalItem {

	@Schema(description = "상품 ID", example = "1")
	private Long itemId;

	@Schema(description = "상품 이름", example = "맥북 프로 16인치")
	private String itemName;

	@Schema(description = "기존 가격", example = "2990000")
	private Integer originalPrice;

	public TimeDealModalItem(Long itemId, String itemName, Integer originalPrice) {
		this.itemId = itemId;
		this.itemName = itemName;
		this.originalPrice = originalPrice;
	}

	public Long getItemId() { return itemId; }
	public String getItemName() { return itemName; }
	public Integer getOriginalPrice() { return originalPrice; }
}