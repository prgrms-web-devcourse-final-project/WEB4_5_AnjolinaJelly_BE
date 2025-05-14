package com.jelly.zzirit.domain.order.service.pay;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.domain.order.util.PaymentGateway;
import com.jelly.zzirit.domain.order.util.PaymentGatewayResolver;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandRefundService {

	private final PaymentRepository paymentRepository;
	private final PaymentGatewayResolver paymentGatewayResolver;
	private final CommandRefundStatusService commandRefundStatusService;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void refund(Order order, String paymentKey, String reason) {
		Payment payment = paymentRepository.findByPaymentKey(paymentKey)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.PAYMENT_NOT_FOUND));

		try {
			PaymentGateway gateway = paymentGatewayResolver.resolve(order.getProvider());
			gateway.refund(paymentKey, order.getTotalPrice(), reason);

		} catch (Exception e) {
			log.error("환불 처리 중 예외 발생: order={}, reason={}", order.getOrderNumber(), reason, e);
			throw new InvalidOrderException(BaseResponseStatus.ORDER_REFUND_FAILED);

		} finally {
			commandRefundStatusService.markAsRefunded(order, payment);
		}
	}
}