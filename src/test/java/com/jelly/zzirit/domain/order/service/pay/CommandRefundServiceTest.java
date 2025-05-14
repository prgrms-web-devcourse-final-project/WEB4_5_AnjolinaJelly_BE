package com.jelly.zzirit.domain.order.service.pay;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.order.domain.fixture.OrderFixture;
import com.jelly.zzirit.domain.order.domain.fixture.PaymentFixture;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.domain.order.service.order.CommandDiscordService;
import com.jelly.zzirit.domain.order.util.PaymentGateway;
import com.jelly.zzirit.domain.order.util.PaymentGatewayResolver;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

@ExtendWith(MockitoExtension.class)
public class CommandRefundServiceTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private CommandDiscordService commandDiscordService;

	@Mock
	private PaymentGatewayResolver paymentGatewayResolver;

	@Mock
	private CommandRefundStatusService commandRefundStatusService;

	@Mock
	private PaymentGateway mockPaymentGateway;

	@InjectMocks
	private CommandRefundService commandRefundService;

	private Order mockOrder;
	private Payment mockPayment;

	@BeforeEach
	void setUp() {
		mockOrder = OrderFixture.결제된_주문_생성(null);
		mockPayment = PaymentFixture.결제_정보_생성();
	}

	@Test
	void 환불_성공_시_프로세스_확인() {
		String paymentKey = "paymentKey123";
		String reason = "상품 불량";

		when(paymentRepository.findByPaymentKey(paymentKey)).thenReturn(Optional.of(mockPayment));
		when(paymentGatewayResolver.resolve(mockOrder.getProvider())).thenReturn(mockPaymentGateway);

		doNothing().when(mockPaymentGateway).refund(paymentKey, mockOrder.getTotalPrice(), reason);

		commandRefundService.refund(mockOrder, paymentKey, reason);

		verify(mockPaymentGateway, times(1)).refund(paymentKey, mockOrder.getTotalPrice(), reason);
		verify(commandRefundStatusService, times(1)).markAsRefunded(mockOrder, mockPayment);
	}

	@Test
	void 결제_정보_없을_경우_예외_처리() {
		String paymentKey = "paymentKey123";
		String reason = "상품 불량";

		when(paymentRepository.findByPaymentKey(paymentKey)).thenReturn(Optional.empty());

		InvalidOrderException exception = assertThrows(InvalidOrderException.class, () -> {
			commandRefundService.refund(mockOrder, paymentKey, reason);
		});

		assertEquals(BaseResponseStatus.PAYMENT_NOT_FOUND, exception.getStatus());
	}

	@Test
	void 환불_실패_시_디스코드_알림_및_예외처리() {
		String paymentKey = "paymentKey123";
		String reason = "상품 불량";

		when(paymentRepository.findByPaymentKey(paymentKey)).thenReturn(Optional.of(mockPayment));
		when(paymentGatewayResolver.resolve(mockOrder.getProvider())).thenReturn(mockPaymentGateway);
		doThrow(new RuntimeException("환불 실패")).when(mockPaymentGateway).refund(paymentKey, mockOrder.getTotalPrice(), reason);

		InvalidOrderException exception = assertThrows(InvalidOrderException.class, () -> {
			commandRefundService.refund(mockOrder, paymentKey, reason);
		});

		verify(commandDiscordService, times(1)).notifyRefundFailure(mockOrder.getOrderNumber(), paymentKey, mockOrder.getTotalPrice(), "환불 실패");
		assertEquals(BaseResponseStatus.ORDER_REFUND_FAILED, exception.getStatus());
	}
}