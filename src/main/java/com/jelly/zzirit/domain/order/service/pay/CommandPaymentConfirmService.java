package com.jelly.zzirit.domain.order.service.pay;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.order.dto.response.PaymentConfirmResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.domain.order.service.message.OrderConfirmMessage;
import com.jelly.zzirit.domain.order.service.message.OrderConfirmProducer;
import com.jelly.zzirit.domain.order.service.payment.TossPaymentClient;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandPaymentConfirmService {

	private final TossPaymentClient tossPaymentClient;
	private final OrderRepository orderRepository;
	private final PaymentRepository paymentRepository;
	private final OrderConfirmProducer orderConfirmProducer;

	public PaymentConfirmResponse confirmPayment(String paymentKey, String orderNumber, String amount) {
		Order order = orderRepository.findByOrderNumber(orderNumber)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ORDER_NOT_FOUND));

		tossPaymentClient.confirmPayment(paymentKey, orderNumber, amount);

		Payment payment = Payment.of(paymentKey, order);
		paymentRepository.save(payment);

		OrderConfirmMessage message = OrderConfirmMessage.from(order, paymentKey, amount);
		orderConfirmProducer.send(message);

		return new PaymentConfirmResponse(
			order.getOrderNumber(),
			paymentKey,
			order.getTotalPrice().intValue()
		);
	}
}