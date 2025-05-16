package com.jelly.zzirit.domain.order.service.order.manage;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.service.message.OrderConfirmMessage;
import com.jelly.zzirit.domain.order.service.payment.TossPaymentClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommandConfirmService {

	private final TossPaymentClient tossPaymentClient;
	private final CommandOrderService commandOrderService;

	@Transactional
	public void confirm(Order order, OrderConfirmMessage message) {
		PaymentResponse paymentInfo = tossPaymentClient.fetchPaymentInfo(message.getPaymentKey());

		tossPaymentClient.validate(order, paymentInfo, message.getAmount());

		Payment payment = order.getPayment();
		payment.changeStatus(Payment.PaymentStatus.DONE);
		payment.changeMethod(paymentInfo.getMethod());

		commandOrderService.completeOrder(order);
	}
}