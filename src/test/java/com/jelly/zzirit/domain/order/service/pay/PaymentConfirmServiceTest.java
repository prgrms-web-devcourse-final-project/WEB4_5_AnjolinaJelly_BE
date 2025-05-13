package com.jelly.zzirit.domain.order.service.pay;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.order.domain.fixture.OrderFixture;
import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import com.jelly.zzirit.domain.order.service.order.TempOrderService;
import com.jelly.zzirit.domain.order.util.PaymentGateway;
import com.jelly.zzirit.domain.order.util.PaymentGatewayResolver;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;


@ExtendWith(MockitoExtension.class)
public class PaymentConfirmServiceTest {

	@Mock
	private TempOrderService tempOrderService;

	@Mock
	private PaymentGatewayResolver paymentGatewayResolver;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private PaymentGateway mockGateway;

	@Mock
	private PaymentResponse mockPaymentResponse;

	@InjectMocks
	private PaymentConfirmService paymentConfirmService;

	@Test
	void 결제_성공_확인() {
		Order mockOrder = OrderFixture.결제된_주문_생성(null);
		String paymentKey = "paymentKey123";
		String orderNumber = mockOrder.getOrderNumber();
		String amount = "10000";

		when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(mockOrder));
		when(paymentGatewayResolver.resolve(mockOrder.getProvider())).thenReturn(mockGateway);
		when(mockGateway.fetchPaymentInfo(paymentKey)).thenReturn(mockPaymentResponse);

		paymentConfirmService.confirmPayment(paymentKey, orderNumber, amount);

		verify(mockGateway, times(1)).confirmPayment(paymentKey, orderNumber, amount);
		verify(tempOrderService, times(1)).confirmTempOrder(mockPaymentResponse);
	}

	@Test
	void 주문_번호_찾을_수_없음() {
		String paymentKey = "paymentKey123";
		String orderNumber = "INVALID_ORDER";
		String amount = "10000";

		when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.empty());

		InvalidOrderException exception = assertThrows(InvalidOrderException.class, () -> {
			paymentConfirmService.confirmPayment(paymentKey, orderNumber, amount);
		});

		assertEquals(BaseResponseStatus.ORDER_NOT_FOUND, exception.getStatus());
	}

	@Test
	void 결제_수단_등록되지_않음() {
		Order mockOrder = OrderFixture.결제된_주문_생성(null);
		String paymentKey = "paymentKey123";
		String orderNumber = mockOrder.getOrderNumber();
		String amount = "10000";

		when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(mockOrder));
		when(paymentGatewayResolver.resolve(mockOrder.getProvider())).thenThrow(new InvalidOrderException(BaseResponseStatus.UNREGISTERED_PAYMENT_GATEWAY));

		InvalidOrderException exception = assertThrows(InvalidOrderException.class, () -> {
			paymentConfirmService.confirmPayment(paymentKey, orderNumber, amount);
		});

		assertEquals(BaseResponseStatus.UNREGISTERED_PAYMENT_GATEWAY, exception.getStatus());
	}
}
