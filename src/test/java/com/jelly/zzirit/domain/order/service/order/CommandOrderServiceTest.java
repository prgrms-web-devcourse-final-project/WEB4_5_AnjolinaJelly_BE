package com.jelly.zzirit.domain.order.service.order;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.repository.order.OrderRepository;
import com.jelly.zzirit.domain.order.service.order.manage.CommandOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static com.jelly.zzirit.domain.order.entity.OrderStatus.COMPLETED;
import static com.jelly.zzirit.domain.order.entity.OrderStatus.PAID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommandOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CommandOrderService commandOrderService;

    @Test
    void 접수_후_24시간이_지난_주문들을_완료_처리하고_총_개수를_반환한다() {
        // given
        Order expiredOrder = mock(Order.class);
        List<Order> expiredOrders = List.of(expiredOrder);

        when(orderRepository.findAllByStatusAndCreatedAtBefore(eq(PAID), any(LocalDateTime.class)))
            .thenReturn(expiredOrders);

        // when
        int result = commandOrderService.completeExpiredOrders();

        // then
        verify(expiredOrder).changeStatus(COMPLETED);
        assertEquals(expiredOrders.size(), result);
    }

}
