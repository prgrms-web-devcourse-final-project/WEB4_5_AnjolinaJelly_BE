package com.jelly.zzirit.domain.order.service.pay;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.dto.request.PaymentRequest;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.service.order.OrderSequenceGenerator;
import com.jelly.zzirit.domain.order.service.order.TempOrderService;
import com.jelly.zzirit.global.AuthMember;

@ExtendWith(MockitoExtension.class)
class TossPaymentServiceTest {

	@InjectMocks
	private TossPaymentService tossPaymentService;

	@Mock
	private OrderSequenceGenerator orderSequenceGenerator;

	@Mock
	private TempOrderService tempOrderService;

	@Test
	void 정상적으로_주문번호를_반환한다() {
		// given
		Long sequence = 123L;
		String expectedOrderNumber = Order.generateOrderNumber(sequence);
		PaymentRequest dto = mock(PaymentRequest.class);

		Member member = mock(Member.class);
		Order mockOrder = mock(Order.class);

		try (MockedStatic<AuthMember> mockedAuth = Mockito.mockStatic(AuthMember.class)) {
			mockedAuth.when(AuthMember::getAuthUser).thenReturn(member);

			when(orderSequenceGenerator.getTodaySequence()).thenReturn(sequence);
			when(tempOrderService.createTempOrder(dto, member, expectedOrderNumber)).thenReturn(mockOrder);
			when(mockOrder.getOrderNumber()).thenReturn(expectedOrderNumber);

			// when
			String result = tossPaymentService.createOrderAndReturnOrderNumber(dto);

			// then
			assertEquals(expectedOrderNumber, result);
			verify(orderSequenceGenerator).getTodaySequence();
			verify(tempOrderService).createTempOrder(dto, member, expectedOrderNumber);
		}
	}
}