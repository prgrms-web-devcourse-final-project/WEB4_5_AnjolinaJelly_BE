package com.jelly.zzirit.domain.order.service.pay;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.service.payment.TossPaymentClient;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandRefundService {

	private final TossPaymentClient tossPaymentClient;
	private final CommandRefundStatusService commandRefundStatusService;

	@CircuitBreaker(name = "tossPaymentBreaker", fallbackMethod = "handleRefundFailure")
	public void refund(Order order, String paymentKey, String reason) {
		boolean isRefundSuccessful = false;

		try {
			tossPaymentClient.refund(paymentKey, order.getTotalPrice(), reason);
			isRefundSuccessful = true;
		} finally {
			commandRefundStatusService.markAsRefunded(order, paymentKey, isRefundSuccessful);
		}
	}

	@SuppressWarnings("unused")
	private void handleRefundFailure(Order order, String paymentKey, String reason, Throwable e) {
		commandRefundStatusService.markAsRefunded(order, paymentKey, false);
		throw new InvalidOrderException(BaseResponseStatus.ORDER_REFUND_FAILED);
	}
}