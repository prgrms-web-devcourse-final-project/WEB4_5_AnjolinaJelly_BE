package com.jelly.zzirit.domain.order.service.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.OrderItem;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderManager {

	private final ItemStockService itemStockService;

	@Transactional(
		isolation = Isolation.READ_COMMITTED,
		timeout = 5
	)
	public void process(Order order) {
		for (OrderItem orderItem : order.getOrderItems()) {
			itemStockService.decrease(orderItem.getItem().getId(), orderItem.getQuantity());
		}

		order.changeStatus(Order.OrderStatus.PAID);
	}
}