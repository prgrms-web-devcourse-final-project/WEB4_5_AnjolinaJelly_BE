package com.jelly.zzirit.domain.order.service.order.manage;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.service.message.OrderConfirmMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommandConfirmService {

	private final CommandOrderService commandOrderService;

	@Transactional
	public void confirm(Order order, OrderConfirmMessage message) {
		Payment payment = order.getPayment();
		payment.changeStatus(Payment.PaymentStatus.DONE);
		payment.changeMethod(message.getMethod());

		commandOrderService.completeOrder(order);
	}
}