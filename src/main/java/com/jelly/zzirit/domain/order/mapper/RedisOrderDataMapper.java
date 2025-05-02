package com.jelly.zzirit.domain.order.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.dto.request.PaymentRequestDto;
import com.jelly.zzirit.domain.order.dto.request.RedisOrderData;

@Component
public class RedisOrderDataMapper {

	public RedisOrderData from(PaymentRequestDto dto, Member member) {
		List<RedisOrderData.ItemData> itemDataList = dto.orderItems().stream()
			.map(item -> new RedisOrderData.ItemData(
				item.itemId(),
				item.timeDealItemId(),
				item.quantity(),
				item.itemName(),
				item.price()
			))
			.toList();

		return new RedisOrderData(
			member,
			dto.totalAmount(),
			dto.shippingAddress(),
			dto.shippingAddressDetail(),
			itemDataList
		);
	}
}