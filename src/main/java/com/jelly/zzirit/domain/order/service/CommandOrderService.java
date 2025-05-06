package com.jelly.zzirit.domain.order.service;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jelly.zzirit.global.dto.BaseResponseStatus.ORDER_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandOrderService {

	private final OrderRepository orderRepository;

	/**
	 * 결제 취소 시도 후 주문 상태 및 결제 상태 변경
	 * @param orderId 취소할 주문의 아이디
	 * @param isRefundSuccessful 결제 취소 성공 여부
	 */
	@Transactional
	public void updateOrderAndPaymentStatusAfterRefund(Long orderId, boolean isRefundSuccessful) {
		Order order = orderRepository.findByIdWithPayment(orderId)
			.orElseThrow(() -> new InvalidOrderException(ORDER_NOT_FOUND));

		Payment payment = order.getPayment();

		if (isRefundSuccessful) {
			order.cancel();
			payment.markCancelled();
		} else {
			payment.markFailed();
		}
	}

}
