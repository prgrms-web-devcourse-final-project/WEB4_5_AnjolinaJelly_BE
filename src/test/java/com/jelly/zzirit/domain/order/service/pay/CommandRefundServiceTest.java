package com.jelly.zzirit.domain.order.service.pay;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.domain.fixture.OrderFixture;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.service.payment.TossPaymentClient;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.jelly.zzirit.global.dto.BaseResponseStatus.ORDER_REFUND_FAILED;
import static com.jelly.zzirit.global.dto.BaseResponseStatus.TOSS_REFUND_FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommandRefundServiceTest {

    @Mock
    private TossPaymentClient tossPaymentClient;

    @Mock
    private CommandRefundStatusService commandRefundStatusService;

    @InjectMocks
    private CommandRefundService commandRefundService;

    private Order order;

    @BeforeEach
    void setUp() {
        Member member = mock(Member.class);
        order = OrderFixture.결제된_주문_생성(member);
    }

    @Test
    void 환불이_성공하면_환불_성공으로_처리된다() {
        // given
        String paymentKey = "pay_abc123";
        String cancelReason = "사용자 주문 취소";

        // when
        commandRefundService.refund(order, paymentKey, cancelReason);

        // then
        verify(tossPaymentClient).refund(paymentKey, order.getTotalPrice(), cancelReason);
        verify(commandRefundStatusService).markAsRefunded(order, paymentKey, true);
    }

    @Test
    void 환불이_실패하면_예외를_던지고_환불_실패로_처리된다() {
        // given
        String paymentKey = "pay_xyz456";
        String cancelReason = "사용자 주문 취소";

        doThrow(new InvalidOrderException(TOSS_REFUND_FAILED))
            .when(tossPaymentClient)
            .refund(paymentKey, order.getTotalPrice(), cancelReason);

        // when & then
        InvalidOrderException exception = assertThrows(
            InvalidOrderException.class,
            () -> commandRefundService.refund(order, paymentKey, cancelReason)
        );

        assertEquals(ORDER_REFUND_FAILED, exception.getStatus());
        verify(commandRefundStatusService).markAsRefunded(order, paymentKey, false);
    }

}