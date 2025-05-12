package com.jelly.zzirit.domain.order.service.order;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.service.pay.RefundService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderManager orderManager;
	private final RefundService refundService;

	public void completeOrder(Order order, String paymentKey) {
		try {
			orderManager.process(order);
		} catch (Exception e) {
			log.error("주문 처리 실패 - 자동 환불 시작: orderNumber={}", order.getOrderNumber(), e);
			refundService.refund(order, paymentKey, "주문 처리 실패로 인한 자동 환불");
		}
	}
}