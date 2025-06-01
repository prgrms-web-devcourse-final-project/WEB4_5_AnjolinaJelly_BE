package com.jelly.zzirit.domain.order.service.message;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.service.order.manage.CommandConfirmService;
import com.jelly.zzirit.domain.order.service.pay.CommandRefundService;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConfirmConsumer {

	private final CommandRefundService commandRefundService;
	private final CommandConfirmService commandConfirmService;

	@RabbitListener(queues = "${rabbitmq.queue.name}")
	public void handle(OrderConfirmMessage message) {
		try {
			commandConfirmService.confirmWithTx(message.getOrderNumber(), message);
		} catch (InvalidOrderException e) {
			throw new AmqpRejectAndDontRequeueException("비즈니스 예외로 인한 재시도 금지", e);
		} catch (Exception e) {
			try {
				Order order = commandConfirmService.findOrderOrThrow(message.getOrderNumber());
				commandRefundService.refund(order, message.getPaymentKey(), "주문 확정 실패");
			} catch (Exception refundEx) {
				log.error("환불 실패 - 주문번호={}, 결제키={}, 사유={}", message.getOrderNumber(), message.getPaymentKey(), refundEx.getMessage(), refundEx);
			}
			throw e;
		}
	}
}