package com.jelly.zzirit.domain.order.service.pay;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

	@InjectMocks
	private RefundService refundService;

	@Mock
	private RestTemplate restTemplate;
	@Mock private PaymentRepository paymentRepository;

	@BeforeEach
	void setup() throws Exception {
		Field secretKeyField = RefundService.class.getDeclaredField("secretKey");
		secretKeyField.setAccessible(true);
		secretKeyField.set(refundService, "test_sk_26DlbXAaV06zWDPZljpb8qY50Q9R");
	}

	@Test
	void 정상환불이면_상태변경과_로그가_발생한다() {
		// given
		String paymentKey = "pay_123";
		BigDecimal amount = new BigDecimal("10000");

		Order order = mock(Order.class);
		Payment payment = mock(Payment.class);

		// 여기서 명확히 연결
		when(payment.getOrder()).thenReturn(order);
		when(paymentRepository.findByPaymentKey(paymentKey)).thenReturn(Optional.of(payment));

		ResponseEntity<String> mockResponse = ResponseEntity.ok("OK");
		when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
			.thenReturn(mockResponse);

		// when
		refundService.refundImmediately(paymentKey, amount);

		// then
		verify(order).changeStatus(Order.OrderStatus.FAILED);
		verify(payment).changeStatus(Payment.PaymentStatus.FAILED);
	}

	@Test
	void 결제정보가_없으면_예외발생() {
		// given
		when(paymentRepository.findByPaymentKey("missing-key")).thenReturn(Optional.empty());

		// when & then
		assertThrows(InvalidOrderException.class, () ->
			refundService.refundImmediately("missing-key", BigDecimal.TEN));
	}

	@Test
	void 환불요청이_실패하면_예외발생() {
		// given
		String paymentKey = "pay_456";
		BigDecimal amount = new BigDecimal("5000");

		Order order = mock(Order.class);
		Payment payment = mock(Payment.class);

		when(paymentRepository.findByPaymentKey(paymentKey)).thenReturn(Optional.of(payment));
		when(payment.getOrder()).thenReturn(order);

		ResponseEntity<String> failResponse = new ResponseEntity<>("Fail", HttpStatus.BAD_REQUEST);
		when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
			.thenReturn(failResponse);

		// when & then
		assertThrows(InvalidOrderException.class, () ->
			refundService.refundImmediately(paymentKey, amount));
	}
}