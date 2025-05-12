package com.jelly.zzirit.domain.order.service.pay;


import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.domain.order.service.order.DiscordNotifier;
import com.jelly.zzirit.domain.order.util.PaymentGateway;
import com.jelly.zzirit.domain.order.util.PaymentGatewayResolver;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

	private final PaymentRepository paymentRepository;
	private final DiscordNotifier discordNotifier;
	private final PaymentGatewayResolver paymentGatewayResolver;
	private final RefundStatusService refundStatusService;

	public void refund(Order order, String paymentKey, String reason) {
		Payment payment = paymentRepository.findByPaymentKey(paymentKey)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.PAYMENT_NOT_FOUND));

		try {
			PaymentGateway gateway = paymentGatewayResolver.resolve(order.getProvider());
			gateway.refund(paymentKey, order.getTotalPrice(), reason);

		} catch (Exception e) {
			notifyDiscordFailure(order.getOrderNumber(), paymentKey, order.getTotalPrice(), e);
			throw new InvalidOrderException(BaseResponseStatus.ORDER_REFUND_FAILED);

		} finally {
			refundStatusService.markAsRefunded(order, payment);
		}
	}

	private void notifyDiscordFailure(String orderNumber, String paymentKey, BigDecimal amount, Exception e) {
		discordNotifier.notifyRefundFailure(orderNumber, paymentKey, amount, e.getMessage());
	}
}