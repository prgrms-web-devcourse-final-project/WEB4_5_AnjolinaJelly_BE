package com.jelly.zzirit.domain.order.service.pay;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.jelly.zzirit.domain.member.domain.MemberFixture;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.domain.fixture.OrderFixture;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.OrderStatus;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.domain.order.repository.order.OrderRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

@ExtendWith(MockitoExtension.class)
class CommandRefundStatusServiceTest {

	@InjectMocks
	private CommandRefundStatusService service;

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private OrderRepository orderRepository;

	private final String paymentKey = "pay_abc123";

	@Test
	void 환불_성공시_ORDER와_PAYMENT가_CANCELLED로_변경된다() {
		try (MockedStatic<TransactionSynchronizationManager> mockedSyncManager = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
			mockedSyncManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
			mockedSyncManager.when(() -> TransactionSynchronizationManager.registerSynchronization(any())).thenAnswer(invocation -> null);

			// given
			Member member = MemberFixture.일반_회원();
			Order order = OrderFixture.결제된_주문_생성(member);
			Payment payment = order.getPayment();

			when(paymentRepository.findByPaymentKey(paymentKey)).thenReturn(Optional.of(payment));

			// when
			service.markAsRefunded(order, paymentKey, true);

			// then
			assertEquals(OrderStatus.CANCELLED, order.getStatus());
			assertEquals(Payment.PaymentStatus.CANCELLED, payment.getPaymentStatus());
			verify(orderRepository).save(order);
			verify(paymentRepository).save(payment);
		}
	}

	@Test
	void 환불_실패시_ORDER와_PAYMENT가_FAILED로_변경된다() {
		try (MockedStatic<TransactionSynchronizationManager> mockedSyncManager = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
			mockedSyncManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
			mockedSyncManager.when(() -> TransactionSynchronizationManager.registerSynchronization(any())).thenAnswer(invocation -> null);

			// given
			Member member = MemberFixture.일반_회원();
			Order order = OrderFixture.결제된_주문_생성(member);
			Payment payment = order.getPayment();

			when(paymentRepository.findByPaymentKey(paymentKey)).thenReturn(Optional.of(payment));

			// when
			service.markAsRefunded(order, paymentKey, false);

			// then
			assertEquals(OrderStatus.FAILED, order.getStatus());
			assertEquals(Payment.PaymentStatus.FAILED, payment.getPaymentStatus());
			verify(orderRepository).save(order);
			verify(paymentRepository).save(payment);
		}
	}

	@Test
	void paymentKey가_존재하지_않으면_예외를_던진다() {
		// given
		Member member = MemberFixture.일반_회원();
		Order order = OrderFixture.결제된_주문_생성(member);
		when(paymentRepository.findByPaymentKey(paymentKey)).thenReturn(Optional.empty());

		// when & then
		InvalidOrderException ex = assertThrows(InvalidOrderException.class, () ->
			service.markAsRefunded(order, paymentKey, true)
		);
		assertEquals(BaseResponseStatus.PAYMENT_NOT_FOUND, ex.getStatus());
		verify(orderRepository, never()).save(any());
		verify(paymentRepository, never()).save(any());
	}
}
