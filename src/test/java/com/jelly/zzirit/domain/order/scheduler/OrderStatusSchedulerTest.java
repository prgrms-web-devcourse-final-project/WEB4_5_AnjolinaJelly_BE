package com.jelly.zzirit.domain.order.scheduler;

import com.jelly.zzirit.domain.order.service.order.manage.CommandOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderStatusSchedulerTest {

    @Mock
    CommandOrderService commandOrderService;

    @InjectMocks
    OrderStatusScheduler orderStatusScheduler;

    @Test
    void 스케줄러가_호출되면_주문이_완료_처리된다() {
        // given
        given(commandOrderService.completeExpiredOrders()).willReturn(5);

        // when
        orderStatusScheduler.updatePaidOrdersToCompleted();

        // then
        verify(commandOrderService).completeExpiredOrders();
    }

}
