package com.jelly.zzirit.domain.order.service.pay;

import com.jelly.zzirit.domain.order.dto.response.PaymentConfirmResponse;
import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.domain.order.repository.order.OrderRepository;
import com.jelly.zzirit.domain.order.service.message.OrderConfirmMessage;
import com.jelly.zzirit.domain.order.service.message.OrderConfirmProducer;
import com.jelly.zzirit.domain.order.service.order.CommandDiscordService;
import com.jelly.zzirit.domain.order.service.payment.TossPaymentClient;
import com.jelly.zzirit.domain.order.util.CircuitBreakerMemoryStore;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandPaymentConfirmService {

	private final CommandRefundService commandRefundService;
	private final CommandDiscordService commandDiscordService;
	private final TossPaymentClient tossPaymentClient;
	private final OrderRepository orderRepository;
	private final PaymentRepository paymentRepository;
	private final OrderConfirmProducer orderConfirmProducer;

	@CircuitBreaker(
			name = "tossPaymentBreaker",
			fallbackMethod = "fallbackConfirmPayment"
	)
	@Retryable(
			retryFor = {
					ConnectException.class,
					SocketTimeoutException.class,
					HttpServerErrorException.class
			},
			backoff = @Backoff(delay = 1000, maxDelay = 3000, multiplier = 2),
			recover = "recoverPaymentConfirm"
	)
	@Transactional
	public PaymentConfirmResponse confirmPayment(String paymentKey, String orderNumber, String amount) {
		Order order = orderRepository.findByOrderNumber(orderNumber)
				.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ORDER_NOT_FOUND));

		// 1. 결제 승인
		tossPaymentClient.confirmPayment(paymentKey, orderNumber, amount);

		// 2. 결제 정보 조회 + 검증
		PaymentResponse paymentInfo = tossPaymentClient.fetchPaymentInfo(paymentKey);
		tossPaymentClient.validate(order, paymentInfo, amount);

		// 3. 결제 저장
		Payment payment = Payment.of(paymentKey, order);
		payment.changeMethod(paymentInfo.getMethod());
		paymentRepository.save(payment);

		OrderConfirmMessage message = OrderConfirmMessage.from(order, paymentKey, amount, paymentInfo.getMethod());
		orderConfirmProducer.send(message);

		return PaymentConfirmResponse.from(order, paymentKey);
	}

	@SuppressWarnings("unused")
	public PaymentConfirmResponse fallbackConfirmPayment(Throwable t, String paymentKey, String orderNumber, String amount) {
		log.error("Toss 결제 승인 요청 차단됨 (CircuitBreaker OPEN): paymentKey={}, error={}", paymentKey, t.getMessage());
		CircuitBreakerMemoryStore.saveFailedPaymentKey(paymentKey);
		throw new InvalidOrderException(BaseResponseStatus.TOSS_CONFIRM_FAILED);
	}

	@Recover
	@SuppressWarnings("unused")
	public PaymentConfirmResponse recoverPaymentConfirm(Exception e, String paymentKey, String orderNumber, String amount) {
		Order order = orderRepository.findByOrderNumber(orderNumber)
				.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ORDER_NOT_FOUND));

		try {
			commandRefundService.refund(order, paymentKey, "결제 승인 확인 실패로 인한 자동 환불");
		} catch (Exception refundEx) {
			commandDiscordService.notifyRefundFailure(
					orderNumber,
					paymentKey,
					order.getTotalPrice(),
					"결제 승인 확인 실패로 인한 자동 환불 실패: " + refundEx.getMessage()
			);
			throw new InvalidOrderException(BaseResponseStatus.ORDER_REFUND_FAILED);
		}

		throw new InvalidOrderException(BaseResponseStatus.TOSS_CONFIRM_FAILED);
	}
}