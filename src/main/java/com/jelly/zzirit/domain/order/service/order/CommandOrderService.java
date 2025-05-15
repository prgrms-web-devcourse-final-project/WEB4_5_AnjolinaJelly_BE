package com.jelly.zzirit.domain.order.service.order;

import static com.jelly.zzirit.domain.order.entity.OrderStatus.*;
import static com.jelly.zzirit.global.dto.BaseResponseStatus.*;
import static org.springframework.transaction.annotation.Isolation.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.OrderStatus;
import com.jelly.zzirit.domain.order.entity.Payment;
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

	/**
	 * 결제 취소 시도 후 주문 상태 및 결제 상태 변경
	 * @param orderId 취소할 주문의 아이디
	 * @param isRefundSuccessful 결제 취소 성공 여부
	 */
	@Transactional
	public void applyRefundResult(Long orderId, boolean isRefundSuccessful) {
		Order order = orderRepository.findByIdWithPayment(orderId)
			.orElseThrow(() -> new InvalidOrderException(ORDER_NOT_FOUND));

		Payment payment = order.getPayment();

		if (isRefundSuccessful) {
			order.markCancelled();
			payment.markCancelled();
		} else {
			order.markPaid();
			payment.markFailed();
		}
	}

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

	@Transactional(isolation = READ_COMMITTED, timeout = 10)
	public void completeOrder(Order order) {
		order.getOrderItems().forEach(item ->
			commandStockService.decrease(item.getItem().getId(), item.getQuantity())
		);
		order.changeStatus(OrderStatus.PAID);
	}
}