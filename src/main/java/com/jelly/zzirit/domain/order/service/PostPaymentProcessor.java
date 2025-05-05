package com.jelly.zzirit.domain.order.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.order.dto.request.RedisOrderData;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.OrderItem;
import com.jelly.zzirit.domain.order.repository.ItemStockRepository;
import com.jelly.zzirit.domain.order.repository.OrderItemRepository;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
//import com.jelly.zzirit.domain.order.repository.TimeDealStockRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostPaymentProcessor {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final ItemStockRepository itemStockRepository;
//	private final TimeDealStockRepository timeDealStockRepository;

	public void process(String orderId, RedisOrderData cached) {
		// 금액 위조 검증
		BigDecimal expectedAmount = cached.getItems().stream()
			.map(RedisOrderData.ItemData::getPrice)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		if (expectedAmount.compareTo(cached.getTotalAmount()) != 0) {
			throw new InvalidOrderException(BaseResponseStatus.PRICE_MANIPULATION_DETECTED);
		}

		// 주문 생성 및 저장
		Order order = Order.of(
			cached.getMember(),
			orderId,
			cached.getTotalAmount(),
			cached.getShippingRequest()
		);
		orderRepository.save(order);

		// 주문 아이템 생성 + 재고 확정
		for (RedisOrderData.ItemData item : cached.getItems()) {
			Item itemEntity = findItem(item.getItemId());
			TimeDealItem timeDealItemEntity = null;
//					(item.getTimeDealItemId() != null)
//				? findTimeDealItem(item.getTimeDealItemId())
//				: null;

			OrderItem orderItem = OrderItem.of(
				order,
				itemEntity,
				item.getQuantity(),
				item.getPrice()
			);

			order.addOrderItem(orderItem);
			orderItemRepository.save(orderItem);

			int updated = itemStockRepository.confirmStock(itemEntity.getId(), item.getQuantity());
//			(timeDealItemEntity != null)
//				? timeDealStockRepository.confirmStock(timeDealItemEntity.getId(), item.getQuantity())
//				:

			if (updated == 0) {
				throw new InvalidOrderException(BaseResponseStatus.STOCK_CONFIRMATION_FAILED);
			}
		}
	}

	private Item findItem(Long itemId) {
		return itemStockRepository.findItemById(itemId)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ITEM_NOT_FOUND));
	}

//	private TimeDealItem findTimeDealItem(Long timeDealItemId) {
//		return timeDealStockRepository.findTimeDealItemById(timeDealItemId)
//			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ITEM_NOT_FOUND));
//	}
}