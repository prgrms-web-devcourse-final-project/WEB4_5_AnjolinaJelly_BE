package com.jelly.zzirit.domain.order.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.jelly.zzirit.domain.member.entity.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedisOrderData {
	private Member member;
	private BigDecimal totalAmount;
	private String shippingRequest;
	private String shippingAddressDetail;
	private List<ItemData> items;

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ItemData {
		private Long itemId;
		private Long timeDealItemId;
		private int quantity;
		private String itemName;
		private BigDecimal price;

		public boolean isTimeDeal() {
			return timeDealItemId != null;
		}

		public Long getTargetStockId() {
			return isTimeDeal() ? timeDealItemId : itemId;
		}
	}
}