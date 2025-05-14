package com.jelly.zzirit.domain.order.service.pay;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import com.jelly.zzirit.domain.order.service.message.OrderConfirmMessage;
import com.jelly.zzirit.domain.order.service.message.OrderConfirmProducer;
import com.jelly.zzirit.domain.order.util.PaymentGateway;
import com.jelly.zzirit.domain.order.util.PaymentGatewayResolver;
import com.jelly.zzirit.domain.order.util.PaymentProvider;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConfirmService {

	private final PaymentGatewayResolver paymentGatewayResolver;
	private final OrderRepository orderRepository;
	private final OrderConfirmProducer orderConfirmProducer;

	public void confirmPayment(String paymentKey, String orderNumber, String amount) {
		Order order = orderRepository.findByOrderNumber(orderNumber)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ORDER_NOT_FOUND));

		PaymentProvider provider = PaymentProvider.TOSS;
		PaymentGateway gateway = paymentGatewayResolver.resolve(provider);
		gateway.confirmPayment(paymentKey, orderNumber, amount);

		order.setProvider(provider);

		OrderConfirmMessage message = OrderConfirmMessage.from(order, paymentKey, amount);
		orderConfirmProducer.send(message);
	}
}