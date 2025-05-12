package com.jelly.zzirit.domain.order.service.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.dto.request.PaymentRequestDto;
import com.jelly.zzirit.domain.order.dto.response.TossPaymentResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.mapper.OrderMapper;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.domain.order.service.pay.TossPaymentValidation;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TempOrderService {

	private final OrderRepository orderRepository;
	private final PaymentRepository paymentRepository;
	private final OrderService orderService;
	private final OrderMapper orderMapper;

	@Transactional
	public Order createTempOrder(PaymentRequestDto dto, Member member, String orderNumber) {
		Order tempOrder = orderMapper.mapToTempOrder(dto, member, orderNumber);
		orderMapper.mapToOrderItems(tempOrder, dto.orderItems());
		orderRepository.save(tempOrder);
		return tempOrder;
	} //임시 주문 생성

	@Transactional
	public void confirmTempOrder(TossPaymentResponse paymentInfo) {
		Order order = orderRepository.findByOrderNumber(paymentInfo.getOrderId())
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ORDER_NOT_FOUND));

		TossPaymentValidation.validateAll(order, paymentInfo, paymentInfo.getTotalAmount().toPlainString());

		Payment payment = Payment.of(paymentInfo.getPaymentKey(), paymentInfo.getMethod());
		paymentRepository.save(payment);

		orderService.completeOrder(order, paymentInfo.getPaymentKey());
	} // 결제 성공 시 주문 확정

	@Transactional
	public void deleteTempOrder(String orderId) {
		Order order = orderRepository.findByOrderNumber(orderId)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ORDER_NOT_FOUND));

		if (order.isConfirmed()) {
			throw new InvalidOrderException(BaseResponseStatus.ALREADY_PROCESSED);
		}

		// paymentRepository.findByOrder(order).ifPresent(paymentRepository::delete);
		orderRepository.delete(order);
	} // 결제 실패 또는 취소 시 임시 주문 제거
}