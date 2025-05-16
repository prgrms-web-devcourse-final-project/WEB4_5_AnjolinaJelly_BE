package com.jelly.zzirit.domain.order.service.order;

import static com.jelly.zzirit.global.dto.BaseResponseStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.service.order.cancel.OrderCancelValidator;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderCancelValidatorTest {

    private final OrderCancelValidator orderCancelValidator = new OrderCancelValidator();

    @Mock
    private Order order;

    @Mock
    private Member member;

    @Test
    void 주문자_본인이면_정상적으로_취소_가능하다() {
        // given
        Long memberId = 1L;

        when(member.getRole()).thenReturn(Role.ROLE_USER);
        when(member.getId()).thenReturn(memberId);
        when(order.wasOrderedBy(memberId)).thenReturn(true);

        // when & then
        assertDoesNotThrow(() -> orderCancelValidator.validate(order, member));
        verify(order).validateCancellable();
    }

    @Test
    void 관리자면_타인의_주문도_취소할_수_있다() {
        // given
        when(member.getRole()).thenReturn(Role.ROLE_ADMIN);

        // when & then
        assertDoesNotThrow(() -> orderCancelValidator.validate(order, member));
        verify(order).validateCancellable();
    }

    @Test
    void 주문자도_아니고_관리자도_아니면_접근이_거부된다() {
        // given
        Long memberId = 1L;

        when(member.getRole()).thenReturn(Role.ROLE_USER);
        when(member.getId()).thenReturn(memberId);
        when(order.wasOrderedBy(memberId)).thenReturn(false);

        // when & then
        InvalidOrderException exception = assertThrows(
            InvalidOrderException.class,
            () -> orderCancelValidator.validate(order, member)
        );

        assertEquals(ACCESS_DENIED, exception.getStatus());
    }

    @Test
    void 결제_완료_상태인_주문이_아니면_예외가_발생한다() {
        // given
        Long memberId = 1L;

        when(member.getRole()).thenReturn(Role.ROLE_USER);
        when(member.getId()).thenReturn(memberId);
        when(order.wasOrderedBy(memberId)).thenReturn(true);

        doThrow(new InvalidOrderException(NOT_PAID_ORDER))
            .when(order).validateCancellable();

        // when & then
        InvalidOrderException exception = assertThrows(
            InvalidOrderException.class,
            () -> orderCancelValidator.validate(order, member)
        );

        assertEquals(NOT_PAID_ORDER, exception.getStatus());
    }

    @Test
    void 접수_후_24시간_이내의_주문이_아니면_예외가_발생한다() {
        // given
        Long memberId = 1L;

        when(member.getRole()).thenReturn(Role.ROLE_USER);
        when(member.getId()).thenReturn(memberId);
        when(order.wasOrderedBy(memberId)).thenReturn(true);

        doThrow(new InvalidOrderException(EXPIRED_CANCEL_TIME))
            .when(order).validateCancellable();

        // when & then
        InvalidOrderException exception = assertThrows(
            InvalidOrderException.class,
            () -> orderCancelValidator.validate(order, member)
        );

        assertEquals(EXPIRED_CANCEL_TIME, exception.getStatus());
    }

}
