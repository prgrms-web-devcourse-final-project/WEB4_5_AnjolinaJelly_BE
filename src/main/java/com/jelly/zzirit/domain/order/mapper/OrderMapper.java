package com.jelly.zzirit.domain.order.mapper;

import java.math.BigDecimal;
import java.util.List;

import com.jelly.zzirit.domain.order.dto.request.OrderItemCreateRequest;
import com.jelly.zzirit.domain.order.dto.request.PaymentRequest;
import com.jelly.zzirit.domain.order.entity.OrderStatus;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.OrderItem;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderMapper {

	private final ItemRepository itemRepository;

	public Order mapToTempOrder(PaymentRequest dto, Member member, String orderNumber) {
		return Order.builder()
			.member(member)
			.orderNumber(orderNumber)
			.totalPrice(BigDecimal.valueOf(dto.totalAmount()))
			.status(OrderStatus.PENDING)
			.shippingRequest(dto.shippingRequest())
			.address(dto.address())
			.addressDetail(dto.addressDetail())
			.build();
	}

	public void mapToOrderItems(Order order, List<OrderItemCreateRequest> itemDtos) {
		List<Long> itemIds = itemDtos.stream()
			.map(OrderItemCreateRequest::itemId)
			.distinct()
			.toList();

		List<Item> items = itemRepository.findAllById(itemIds);

		for (OrderItemCreateRequest dto : itemDtos) {
			Item item = items.stream()
				.filter(i -> i.getId().equals(dto.itemId()))
				.findFirst()
				.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ITEM_NOT_FOUND));

			order.addOrderItem(OrderItem.of(order, item, dto.quantity(), item.getPrice()));
		}
	}
}