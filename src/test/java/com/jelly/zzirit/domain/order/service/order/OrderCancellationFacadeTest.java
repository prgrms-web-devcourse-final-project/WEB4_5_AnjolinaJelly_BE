package com.jelly.zzirit.domain.order.service.order;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.OrderItem;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.repository.order.OrderRepository;
import com.jelly.zzirit.domain.order.service.order.cancel.OrderCancelValidator;
import com.jelly.zzirit.domain.order.service.order.cancel.OrderCancellationFacade;
import com.jelly.zzirit.domain.order.service.pay.CommandRefundService;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.jelly.zzirit.global.dto.BaseResponseStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCancellationFacadeTest {

    @Mock
    private OrderCancelValidator orderCancelValidator;

    @Mock
    private CommandRefundService commandRefundService;

    @Mock
    private CommandStockService commandStockService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderCancellationFacade orderCancellationFacade;

    @Mock
    private Order order;

    @Mock
    private Payment payment;

    @Mock
    private Member member;

    @Mock
    private OrderItem orderItem;

    @Mock
    private Item item;

    @Test
    void 주문_취소와_환불이_정상적으로_처리되어_재고가_복구된다() {
        // given
        Long orderId = 1L;
        Long itemId = 42L;
        int itemQuantity = 3;
        String paymentKey = "payment-key";
        String cancelReason = "사용자 주문 취소";

        when(orderRepository.findByIdWithPayment(orderId)).thenReturn(Optional.of(order));
        doNothing().when(orderCancelValidator).validate(order, member);

        when(order.getPayment()).thenReturn(payment);
        when(payment.getPaymentKey()).thenReturn(paymentKey);
        doNothing().when(commandRefundService).refund(order, paymentKey, cancelReason);

        when(order.getOrderItems()).thenReturn(List.of(orderItem));
        when(orderItem.getItem()).thenReturn(item);
        when(orderItem.getQuantity()).thenReturn(itemQuantity);
        when(item.getId()).thenReturn(itemId);
        doNothing().when(commandStockService).restore(itemId, itemQuantity);

        // when
        orderCancellationFacade.cancelOrderAndRefund(orderId, member);

        // then
        verify(commandRefundService).refund(order, paymentKey, cancelReason);
        verify(commandStockService).restore(itemId, itemQuantity);
    }

    @Test
    void 주문이_존재하지_않으면_예외가_발생한다() {
        // given
        Long orderId = 1L;

        when(orderRepository.findByIdWithPayment(orderId)).thenReturn(Optional.empty());

        // when & then
        InvalidOrderException exception = assertThrows(
            InvalidOrderException.class,
            () -> orderCancellationFacade.cancelOrderAndRefund(orderId, member)
        );

        assertEquals(ORDER_NOT_FOUND, exception.getStatus());
    }

    @Test
    void 주문을_취소할_권한이_없으면_예외가_발생한다() {
        // given
        Long orderId = 1L;

        when(orderRepository.findByIdWithPayment(orderId)).thenReturn(Optional.of(order));
        doThrow(new InvalidOrderException(ACCESS_DENIED)).when(orderCancelValidator).validate(order, member);

        // when & then
        InvalidOrderException exception = assertThrows(
            InvalidOrderException.class,
            () -> orderCancellationFacade.cancelOrderAndRefund(orderId, member)
        );

        assertEquals(ACCESS_DENIED, exception.getStatus());
    }

    @Test
    void 결제_완료_상태인_주문이_아니면_예외가_발생한다() {
        // given
        Long orderId = 1L;

        when(orderRepository.findByIdWithPayment(orderId)).thenReturn(Optional.of(order));
        doThrow(new InvalidOrderException(NOT_PAID_ORDER)).when(orderCancelValidator).validate(order, member);

        // when & then
        InvalidOrderException exception = assertThrows(
            InvalidOrderException.class,
            () -> orderCancellationFacade.cancelOrderAndRefund(orderId, member)
        );

        assertEquals(NOT_PAID_ORDER, exception.getStatus());
    }

    @Test
    void 접수_후_24시간_이내의_주문이_아니면_예외가_발생한다() {
        // given
        Long orderId =1L;

        when(orderRepository.findByIdWithPayment(orderId)).thenReturn(Optional.of(order));
        doThrow(new InvalidOrderException(EXPIRED_CANCEL_TIME)).when(orderCancelValidator).validate(order, member);

        // when & then
        InvalidOrderException exception = assertThrows(
            InvalidOrderException.class,
            () -> orderCancellationFacade.cancelOrderAndRefund(orderId, member)
        );

        assertEquals(EXPIRED_CANCEL_TIME, exception.getStatus());
    }

    @Test
    void 환불이_실패하면_예외가_발생한다() {
        // given
        Long orderId = 1L;
        String paymentKey = "payment-key";
        String cancelReason = "사용자 주문 취소";

        when(orderRepository.findByIdWithPayment(orderId)).thenReturn(Optional.of(order));
        doNothing().when(orderCancelValidator).validate(order, member);

        when(order.getPayment()).thenReturn(payment);
        when(payment.getPaymentKey()).thenReturn(paymentKey);
        doThrow(new InvalidOrderException(ORDER_REFUND_FAILED)).when(commandRefundService).refund(order, paymentKey, cancelReason);

        // when & then
        InvalidOrderException exception = assertThrows(
            InvalidOrderException.class,
            () -> orderCancellationFacade.cancelOrderAndRefund(orderId, member)
        );

        assertEquals(ORDER_REFUND_FAILED, exception.getStatus());
    }

}
