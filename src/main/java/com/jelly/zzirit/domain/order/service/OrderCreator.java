package com.jelly.zzirit.domain.order.service;

import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.order.dto.request.RedisOrderData;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.OrderItem;
import com.jelly.zzirit.domain.order.repository.ItemStockRepository;
import com.jelly.zzirit.domain.order.repository.OrderItemRepository;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import com.jelly.zzirit.domain.order.repository.TimeDealStockRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderCreator {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final ItemStockRepository itemStockRepository;
	private final TimeDealStockRepository timeDealStockRepository;

	public void createOrderWithItems(String orderId, RedisOrderData cached) {
		Order order = Order.of(
			cached.getMember(),
			orderId,
			cached.getTotalAmount(),
			cached.getShippingRequest()
		);
		orderRepository.save(order);

		for (RedisOrderData.ItemData item : cached.getItems()) {
			Item itemEntity = itemStockRepository.findItemById(item.getItemId())
				.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ITEM_NOT_FOUND));

			TimeDealItem timeDealItemEntity = (item.getTimeDealItemId() != null)
				? timeDealStockRepository.findTimeDealItemById(item.getTimeDealItemId())
				.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ITEM_NOT_FOUND))
				: null;

			OrderItem orderItem = OrderItem.of(
				order,
				itemEntity,
				timeDealItemEntity,
				item.getQuantity(),
				item.getPrice()
			);
			orderItemRepository.save(orderItem);
		}
	}
}