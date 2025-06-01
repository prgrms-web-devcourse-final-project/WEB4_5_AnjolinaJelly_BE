package com.jelly.zzirit.domain.order.service.order.manage;

import static com.jelly.zzirit.domain.order.entity.OrderStatus.*;
import static org.springframework.transaction.annotation.Isolation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.cart.service.CartService;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.OrderItem;
import com.jelly.zzirit.domain.order.entity.OrderStatus;
import com.jelly.zzirit.domain.order.repository.order.OrderRepository;
import com.jelly.zzirit.domain.order.service.order.CommandStockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandOrderService {

	private final OrderRepository orderRepository;
	private final CartService cartService;
	private final CommandStockService commandStockService;

	/**
	 * 24시간이 지난 주문을 COMPLETED 상태로 변경
	 * @return 변경된 주문의 개수
	 */
	@Transactional
	public int completeExpiredOrders() {
		LocalDateTime deadline = LocalDateTime.now().minusHours(24);

		// 결제 완료(PAID) 상태이면서 24시간이 지난 주문 목록의 상태 변경
		List<Order> expiredOrders = orderRepository.findAllByStatusAndCreatedAtBefore(PAID, deadline);
		expiredOrders.forEach(order -> order.changeStatus(COMPLETED));

		return expiredOrders.size();
	}

	@Transactional
	public void completeOrder(Order order) {

		for (OrderItem item : order.getOrderItems()) {
			commandStockService.decrease(order.getOrderNumber(), item.getItem().getId(), item.getQuantity());
		}

		order.changeStatus(OrderStatus.PAID);

		List<Item> orderedItems = order.getOrderItems().stream()
				.map(OrderItem::getItem)
				.collect(Collectors.toList());

		cartService.removeOrderedItemsFromCart(order.getMember(), orderedItems);
	}
}