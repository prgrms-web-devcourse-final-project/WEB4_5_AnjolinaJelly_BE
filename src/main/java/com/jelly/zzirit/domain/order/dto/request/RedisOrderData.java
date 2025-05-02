package com.jelly.zzirit.domain.order.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.jelly.zzirit.domain.member.entity.Member;

public record RedisOrderData(
	Member member,
	BigDecimal totalAmount,
	String shippingRequest,
	String shippingAddressDetail,
	List<ItemData> items
) {
	public record ItemData(
		Long itemId,
		Long timeDealItemId,
		int quantity,
		String itemName,
		BigDecimal price
	) {}
}