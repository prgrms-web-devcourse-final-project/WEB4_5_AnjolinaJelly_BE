package com.jelly.zzirit.domain.order.service.pay;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;

@Service
public class RefundStatusService {

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markAsRefunded(Order order, Payment payment) {
		order.changeStatus(Order.OrderStatus.FAILED);
		payment.changeStatus(Payment.PaymentStatus.FAILED);
	}
}