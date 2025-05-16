package com.jelly.zzirit.domain.order.service.message;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class OrderConfirmProducer {

	private final RabbitTemplate rabbitTemplate;

	@Value("${rabbitmq.exchange.name}")
	private String exchange;

	@Value("${rabbitmq.routing.key}")
	private String routingKey;

	public void send(OrderConfirmMessage message) {
		rabbitTemplate.convertAndSend(exchange, routingKey, message);
	}
}