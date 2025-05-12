package com.jelly.zzirit.domain.order.service.pay;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.order.dto.request.PaymentRequest;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.service.order.OrderSequenceGenerator;
import com.jelly.zzirit.domain.order.service.order.TempOrderService;
import com.jelly.zzirit.global.AuthMember;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentInitService {

	private final OrderSequenceGenerator orderSequenceGenerator;
	private final TempOrderService tempOrderService;

	public String createOrderAndReturnOrderNumber(PaymentRequest dto) {
		Long todaySequence = orderSequenceGenerator.getTodaySequence();
		String orderNumber = Order.generateOrderNumber(todaySequence);

		Order order = tempOrderService.createTempOrder(dto, AuthMember.getAuthUser(), orderNumber);
		return order.getOrderNumber();
	}
}