package com.jelly.zzirit.domain.order.service.order;

import static com.jelly.zzirit.global.dto.BaseResponseStatus.*;
import static org.springframework.transaction.annotation.Isolation.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandOrderService {

	private final OrderRepository orderRepository;
	private final CommandStockService commandStockService;

	@Transactional
	public void applyRefundResult(Long orderId, boolean isRefundSuccessful) {
		Order order = getOrderWithPayment(orderId);

		if (isRefundSuccessful) {
			markOrderAndPaymentAsCancelled(order);
		} else {
			order.getPayment().markFailed();
		}
	}

	private void markOrderAndPaymentAsCancelled(Order order) {
		order.cancel();
		order.getPayment().markCancelled();
	}

	private Order getOrderWithPayment(Long orderId) {
		return orderRepository.findByIdWithPayment(orderId)
			.orElseThrow(() -> new InvalidOrderException(ORDER_NOT_FOUND));
	}

	@Transactional(isolation = READ_COMMITTED, timeout = 10)
	public void completeOrder(Order order) {
		order.getOrderItems().forEach(item ->
			commandStockService.decrease(item.getItem().getId(), item.getQuantity())
		);
		order.changeStatus(Order.OrderStatus.PAID);
	}
}