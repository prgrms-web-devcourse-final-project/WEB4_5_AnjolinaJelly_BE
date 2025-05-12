package com.jelly.zzirit.domain.order.service.pay;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import com.jelly.zzirit.domain.order.service.order.TempOrderService;
import com.jelly.zzirit.domain.order.util.PaymentGateway;
import com.jelly.zzirit.domain.order.util.PaymentGatewayResolver;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConfirmService {

	private final TempOrderService tempOrderService;
	private final PaymentGatewayResolver paymentGatewayResolver;
	private final OrderRepository orderRepository;

	public void confirmPayment(String paymentKey, String orderNumber, String amount) {

		Order order = orderRepository.findByOrderNumber(orderNumber)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ORDER_NOT_FOUND));

		PaymentGateway gateway = paymentGatewayResolver.resolve(order.getProvider());
		gateway.confirmPayment(paymentKey, orderNumber, amount);
		PaymentResponse paymentInfo = gateway.fetchPaymentInfo(paymentKey);
		tempOrderService.confirmTempOrder(paymentInfo);
	}
}