package com.jelly.zzirit.domain.order.service.order;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.repository.order.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QueryOrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    QueryOrderService queryOrderService;

    @Mock
    private Order recentOrder;

    @Mock
    private Order oldOrder;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
    }

    @Test
    void 주문_전체_조회_시_페이징_및_정렬_처리된_결과를_반환한다() {
        // given
        Long memberId = 1L;
        Long oldOrderId = 1L;
        Long recentOrderId = 2L;

        List<Long> orderIds = List.of(recentOrderId, oldOrderId);
        Page<Long> pagedOrderIds = new PageImpl<>(orderIds, pageable, 2);

        when(recentOrder.getId()).thenReturn(recentOrderId);
        when(oldOrder.getId()).thenReturn(oldOrderId);
        when(orderRepository.findOrderIdsByMemberId(eq(memberId), eq(pageable)))
            .thenReturn(pagedOrderIds);
        when(orderRepository.findByIdsWithItems(orderIds))
            .thenReturn(List.of(recentOrder, oldOrder));

        // when
        Page<Order> result = queryOrderService.findPagedOrders(memberId, pageable);

        // then
        assertEquals(2, result.getContent().size());
        assertEquals(recentOrder, result.getContent().get(0));
        assertEquals(oldOrder, result.getContent().get(1));
    }

    @Test
    void 주문_전체_조회_시_주문_목록이_없다면_빈_결과를_반환한다() {
        // given
        Long memberId = 1L;
        Page<Long> pagedOrderIds = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(orderRepository.findOrderIdsByMemberId(eq(memberId), eq(pageable)))
            .thenReturn(pagedOrderIds);

        // when
        Page<Order> result = queryOrderService.findPagedOrders(memberId, pageable);

        // then
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

}
