package com.jelly.zzirit.domain.order.service.pay;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import com.jelly.zzirit.domain.order.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.order.dto.response.TossPaymentResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.util.payment.TossPaymentValidation;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

@ExtendWith(MockitoExtension.class)
class TossPaymentValidationTest {

	@Test
	void 금액이_일치하지_않으면_예외() {
		Order order = mock(Order.class);
		when(order.getTotalPrice()).thenReturn(new BigDecimal("1000"));

		TossPaymentResponse response = mock(TossPaymentResponse.class);

		assertThrows(InvalidOrderException.class, () ->
			TossPaymentValidation.PAYMENT_AMOUNT_MISMATCH.validate(order, response, "900"));
	}

	@Test
	void 주문번호가_다르면_예외() {
		Order order = mock(Order.class);
		when(order.getOrderNumber()).thenReturn("ORDER123");

		TossPaymentResponse response = mock(TossPaymentResponse.class);
		when(response.getOrderId()).thenReturn("WRONG_ORDER");

		assertThrows(InvalidOrderException.class, () ->
			TossPaymentValidation.ORDER_ID_MISMATCH.validate(order, response, "1000"));
	}

	@Test
	void 상태가_PENDING_이_아니면_예외() {
		Order order = mock(Order.class);
		when(order.getStatus()).thenReturn(OrderStatus.COMPLETED);

		TossPaymentResponse response = mock(TossPaymentResponse.class);

		assertThrows(InvalidOrderException.class, () ->
			TossPaymentValidation.ALREADY_PROCESSED.validate(order, response, "1000"));
	}


	@Test
	void PAYMENT_AMOUNT_MISMATCH_금액이_일치하지_않으면_예외() {
		Order order = mock(Order.class);
		when(order.getTotalPrice()).thenReturn(new BigDecimal("1000"));

		TossPaymentResponse response = mock(TossPaymentResponse.class);

		assertThrows(InvalidOrderException.class, () ->
			TossPaymentValidation.PAYMENT_AMOUNT_MISMATCH.validate(order, response, "900"));
	}


	@Test
	void TOSS_AMOUNT_MISMATCH_Toss측_금액이_다르면_예외() {
		Order order = mock(Order.class);

		TossPaymentResponse response = mock(TossPaymentResponse.class);
		when(response.getTotalAmount()).thenReturn(new BigDecimal("1100"));

		assertThrows(InvalidOrderException.class, () ->
			TossPaymentValidation.TOSS_AMOUNT_MISMATCH.validate(order, response, "1000"));
	}

	@Test
	void validateAll_정상입력시_예외없음() {
		Order order = mockOrder("ORDER123", new BigDecimal("1000"), OrderStatus.PENDING);
		TossPaymentResponse response = mockResponse("ORDER123", "DONE", new BigDecimal("1000"));

		assertDoesNotThrow(() ->
			TossPaymentValidation.validateAll(order, response, "1000"));
	}


	private Order mockOrder(String orderNumber, BigDecimal price, OrderStatus status) {
		Order order = mock(Order.class);
		when(order.getOrderNumber()).thenReturn(orderNumber);
		when(order.getTotalPrice()).thenReturn(price);
		when(order.getStatus()).thenReturn(status);
		return order;
	}

	private TossPaymentResponse mockResponse(String orderId, String status, BigDecimal amount) {
		TossPaymentResponse response = mock(TossPaymentResponse.class);
		when(response.getOrderId()).thenReturn(orderId);
		when(response.getStatus()).thenReturn(status);
		when(response.getTotalAmount()).thenReturn(amount);
		return response;
	}
}
