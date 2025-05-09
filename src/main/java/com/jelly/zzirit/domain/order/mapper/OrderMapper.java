package com.jelly.zzirit.domain.order.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.dto.request.OrderItemRequestDto;
import com.jelly.zzirit.domain.order.dto.request.PaymentRequestDto;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.OrderItem;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderMapper {

	private final ItemRepository itemRepository;

	public Order mapToTempOrder(PaymentRequestDto dto, Member member, String orderNumber) {
		return Order.builder()
			.member(member)
			.orderNumber(orderNumber)
			.totalPrice(dto.totalAmount())
			.status(Order.OrderStatus.PENDING)
			.shippingRequest(dto.shippingRequest())
			.address(dto.address())
			.addressDetail(dto.addressDetail())
			.build();
	}

	public void mapToOrderItems(Order order, List<OrderItemRequestDto> itemDtos) {
		List<Long> itemIds = itemDtos.stream()
			.map(OrderItemRequestDto::itemId)
			.distinct()
			.toList();

		List<Item> items = itemRepository.findAllById(itemIds);
		Map<Long, Item> itemMap = new HashMap<>(items.size());
		for (Item item : items) {
			itemMap.put(item.getId(), item);
		}

		for (OrderItemRequestDto dto : itemDtos) {
			Item item = itemMap.get(dto.itemId());
			if (item == null) {
				throw new InvalidOrderException(BaseResponseStatus.ITEM_NOT_FOUND);
			}
			order.addOrderItem(OrderItem.of(order, item, dto.quantity(), dto.price()));
		}
	}
}