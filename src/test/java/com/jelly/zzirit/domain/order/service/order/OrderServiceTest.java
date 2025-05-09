package com.jelly.zzirit.domain.order.service.order;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.service.pay.RefundService;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@InjectMocks
	private CommandOrderService commandOrderService;

	@Mock
	private OrderManager orderManager;
	@Mock private RefundService refundService;

	@Test
	void 정상처리되면_환불되지_않는다() {
		// given
		Order order = mock(Order.class);
		String paymentKey = "pay_001";

		// when
		commandOrderService.completeOrder(order, paymentKey);

		// then
		verify(orderManager).process(order);
		verifyNoInteractions(refundService);
	}

	@Test
	void 처리실패시_환불이_호출된다() {
		// given
		Order order = mock(Order.class);
		String paymentKey = "pay_002";
		BigDecimal totalPrice = new BigDecimal("15000");

		when(order.getOrderNumber()).thenReturn("ORDER-002");
		when(order.getTotalPrice()).thenReturn(totalPrice);
		doThrow(new RuntimeException("처리 실패")).when(orderManager).process(order);

		// when
		commandOrderService.completeOrder(order, paymentKey);

		// then
		verify(refundService).refund(paymentKey, totalPrice, "주문 처리 실패로 인한 자동 환불");
	}
}