package com.jelly.zzirit.domain.order.service;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

import static com.jelly.zzirit.domain.order.entity.Order.OrderStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryOrderService {

    private final OrderRepository orderRepository;

    /**
     * CANCELLED, COMPLETED, PAID 상태인 주문 내역을 최신순으로 조회
     * @param memberId 현재 로그인한 유저의 아이디
     * @return 주문 리스트
     */
    public List<Order> findAllOrders(Long memberId) {
        return orderRepository.findAllByMemberIdWithItems(memberId, EnumSet.of(CANCELLED, COMPLETED, PAID));
    }

}
