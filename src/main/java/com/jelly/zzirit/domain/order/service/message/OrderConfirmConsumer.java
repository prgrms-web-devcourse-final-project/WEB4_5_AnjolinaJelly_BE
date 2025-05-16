package com.jelly.zzirit.domain.order.service.message;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.repository.order.OrderRepository;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.domain.order.service.order.manage.CommandConfirmService;
import com.jelly.zzirit.domain.order.service.pay.CommandRefundService;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConfirmConsumer {

	private final OrderRepository orderRepository;
	private final PaymentRepository paymentRepository;
	private final CommandRefundService commandRefundService;
	private final CommandConfirmService commandConfirmService;

	@RabbitListener(queues = "${rabbitmq.queue.name}")
	public void handle(OrderConfirmMessage message) {
		log.info("주문 확정 메시지 수신: {}", message.getOrderNumber());
		Order order = findOrderOrThrow(message.getOrderNumber());

		if (paymentRepository.existsByPaymentKey(message.getPaymentKey())) {
			return;
		}

		try {
			commandConfirmService.confirm(order, message);

		} catch (InvalidOrderException e) {
			throw new AmqpRejectAndDontRequeueException("비즈니스 예외로 인한 재시도 금지", e);

		} catch (Exception e) {
			commandRefundService.refund(order, message.getPaymentKey(), "주문 확정 실패");
			throw e;
		}
	}

	private Order findOrderOrThrow(String orderNumber) {
		return orderRepository.findByOrderNumber(orderNumber)
			.orElseThrow(() ->
				new AmqpRejectAndDontRequeueException("주문 없음 → 재시도 불필요",
					new InvalidOrderException(BaseResponseStatus.ORDER_NOT_FOUND)));
	}
}