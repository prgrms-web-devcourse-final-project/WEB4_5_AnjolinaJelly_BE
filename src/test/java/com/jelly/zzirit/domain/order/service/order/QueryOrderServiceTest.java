package com.jelly.zzirit.domain.order.service.order;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.repository.OrderRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static com.jelly.zzirit.domain.order.entity.Order.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QueryOrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    QueryOrderService orderService;

    @Test
    @DisplayName("주문 내역이 존재하는 경우 주문 전체를 조회하면 모든 주문을 반환한다")
    void 주문_내역이_존재하는_경우_주문_전체를_조회하면_모든_주문을_반환한다() {
        // given
        Long memberId = 1L;
        EnumSet<Order.OrderStatus> statuses = EnumSet.of(CANCELLED, COMPLETED, PAID);

        List<Order> mockOrders = List.of(mock(Order.class), mock(Order.class), mock(Order.class));

        given(orderRepository.findAllByMemberIdWithItems(memberId, statuses))
            .willReturn(mockOrders);

        // when
        List<Order> result = orderService.findAllOrders(memberId);

        // then
        assertThat(result).hasSize(mockOrders.size());
    }

    @Test
    @DisplayName("주문 내역이 존재하지 않는 경우 주문 전체를 조회하면 빈 리스트를 반환한다")
    void 주문_내역이_존재하지_않는_경우_주문_전체를_조회하면_빈_리스트를_반환한다() {
        // given
        Long memberId = 1L;

        when(orderRepository.findAllByMemberIdWithItems(memberId, EnumSet.of(CANCELLED, COMPLETED, PAID)))
            .thenReturn(Collections.emptyList());

        // when
        List<Order> result = orderService.findAllOrders(memberId);

        // then
        assertThat(result).isEmpty();
    }

}
