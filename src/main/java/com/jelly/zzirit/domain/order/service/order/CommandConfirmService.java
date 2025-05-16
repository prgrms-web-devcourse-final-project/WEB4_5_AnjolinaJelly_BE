package com.jelly.zzirit.domain.order.service.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.domain.order.service.message.OrderConfirmMessage;
import com.jelly.zzirit.domain.order.service.payment.TossPaymentClient;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommandConfirmService {

	private final TossPaymentClient tossPaymentClient;
	private final PaymentRepository paymentRepository;
	private final CommandOrderService commandOrderService;

	@Transactional
	public void confirm(Order order, OrderConfirmMessage message) {
		PaymentResponse paymentInfo = tossPaymentClient.fetchPaymentInfo(message.getPaymentKey());

		tossPaymentClient.validate(order, paymentInfo, message.getAmount());

		Payment payment = paymentRepository.findByPaymentKey(paymentInfo.getPaymentKey())
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.PAYMENT_NOT_FOUND));

		payment.changeStatus(Payment.PaymentStatus.DONE);
		payment.changeMethod(paymentInfo.getMethod());

		paymentRepository.save(payment);
		commandOrderService.completeOrder(order);
	}
}