package com.jelly.zzirit.domain.order.service.pay;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.service.payment.TossPaymentClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandRefundService {

	private final TossPaymentClient tossPaymentClient;
	private final CommandRefundStatusService commandRefundStatusService;

	public void refund(Order order, String paymentKey, String reason) {
		boolean isRefundSuccessful = attemptRefund(order, paymentKey, reason);
		commandRefundStatusService.markAsRefunded(order, paymentKey, isRefundSuccessful);
	}

	private boolean attemptRefund(Order order, String paymentKey, String reason) {
		try {
			tossPaymentClient.refund(paymentKey, order.getTotalPrice(), reason);
			return true;
		} catch (Exception e) {
			log.error("환불 처리 중 예외 발생: order={}, reason={}", order.getOrderNumber(), reason, e);
			return false;
		}
	}
}