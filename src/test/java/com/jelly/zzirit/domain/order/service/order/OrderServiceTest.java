package com.jelly.zzirit.domain.order.service.order;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.domain.fixture.OrderFixture;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.service.pay.CommandRefundService;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private CommandOrderManager commandOrderManager;

	@Mock
	private CommandRefundService commandRefundService;

	@InjectMocks
	private CommandOrderService orderService;

	@Mock
	private Member member;

	@Mock
	private Payment payment;

	@Test
	void completeOrder_정상처리() {
		// given
		Order order = OrderFixture.주문_생성(member, payment);

		// when
		orderService.completeOrder(order, "paymentKey");

		// then
		verify(commandRefundService, never()).refund(any(), any(), any());
	}

	@Test
	void completeOrder_주문처리_실패_환불처리() {
		// given
		Order order = OrderFixture.주문_생성(member, payment);
		String paymentKey = "paymentKey";
		doThrow(new RuntimeException("주문 처리 실패")).when(commandOrderManager).process(order);

		// when
		orderService.completeOrder(order, paymentKey);

		// then
		verify(commandRefundService).refund(eq(order), eq(paymentKey), eq("주문 처리 실패로 인한 자동 환불"));
	}
}