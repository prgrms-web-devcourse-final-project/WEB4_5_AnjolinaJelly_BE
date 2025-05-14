package com.jelly.zzirit.domain.order.service.message;

import java.math.BigDecimal;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.order.service.order.CommandDiscordService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConfirmDLQConsumer {

	private final CommandDiscordService commandDiscordService;

	@RabbitListener(queues = "${rabbitmq.queue.dlq-name}")
	public void handle(OrderConfirmMessage message) {

		commandDiscordService.notifyRefundFailure(
			message.getOrderNumber(),
			message.getPaymentKey(),
			new BigDecimal(message.getAmount()),
			"DLQ 도달 - 주문 확정 및 환불 모두 실패함. 수동 조치 필요"
		);
	}
}