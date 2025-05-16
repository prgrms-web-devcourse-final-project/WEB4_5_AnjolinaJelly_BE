package com.jelly.zzirit.domain.order.service.order.cancel;

import static com.jelly.zzirit.global.dto.BaseResponseStatus.*;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import com.jelly.zzirit.domain.order.service.order.CommandStockService;
import com.jelly.zzirit.domain.order.service.pay.CommandRefundService;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCancellationFacade {

	private final OrderCancelValidator orderCancelValidator;
	private final CommandRefundService refundService;
	private final CommandStockService commandStockService;
	private final OrderRepository orderRepository;

	public void cancelOrderAndRefund(Long orderId, Member member) {
		Order order = orderRepository.findByIdWithPayment(orderId)
			.orElseThrow(() -> new InvalidOrderException(ORDER_NOT_FOUND));

		orderCancelValidator.validate(order, member);

		try {
			refundService.refund(order, order.getPayment().getPaymentKey(), "사용자 주문 취소");
			restoreStock(order);

		} catch (InvalidOrderException e) {
			log.error("환불 실패로 주문 취소가 완료되지 않았습니다. orderId={}", orderId, e);
			throw e;
		}
	}

	private void restoreStock(Order order) {
		order.getOrderItems().forEach(item -> commandStockService.restore(item.getItem().getId(), item.getQuantity()));
	}
}