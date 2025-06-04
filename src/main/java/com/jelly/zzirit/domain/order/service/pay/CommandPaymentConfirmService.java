package com.jelly.zzirit.domain.order.service.pay;

import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.order.dto.response.PaymentConfirmResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.repository.order.OrderRepository;
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

	@Transactional
	public PaymentConfirmResponse confirmPayment(String paymentKey, String orderNumber, String amount) {
		System.out.println("[결제확정요청] 시작 - orderNumber=" + orderNumber + ", paymentKey=" + paymentKey + ", amount=" + amount);

		Order order = orderRepository.findByOrderNumber(orderNumber)
				.orElseThrow(() -> {
					System.out.println("[결제확정요청] 주문 조회 실패 - orderNumber=" + orderNumber);
					return new InvalidOrderException(BaseResponseStatus.ORDER_NOT_FOUND);
				});

		System.out.println("[결제확정요청] 주문 조회 성공 - memberId=" + order.getMember().getId());

		PaymentResponse paymentResponse = tossPaymentClient.confirmPayment(paymentKey, orderNumber, amount);
		System.out.println("[결제확정요청] confirm 응답: method=" + paymentResponse.getMethod()
				+ ", status=" + paymentResponse.getStatus()
				+ ", totalAmount=" + paymentResponse.getTotalAmount());


		Payment payment = Payment.of(paymentKey, order);
		paymentRepository.save(payment);
		paymentRepository.flush();
		System.out.println("[결제확정요청] 결제 정보 저장 완료 - paymentKey=" + paymentKey);

		OrderConfirmMessage message = OrderConfirmMessage.from(order, paymentKey, amount);
		orderConfirmProducer.send(message);
		System.out.println("[결제확정요청] 주문 확정 메시지 발행 완료 - queue=order.confirm.queue");

		return PaymentConfirmResponse.from(order, paymentKey);
	}
}