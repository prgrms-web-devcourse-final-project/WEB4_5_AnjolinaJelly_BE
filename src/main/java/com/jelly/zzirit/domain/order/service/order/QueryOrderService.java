package com.jelly.zzirit.domain.order.service.order;

import static com.jelly.zzirit.domain.order.entity.OrderStatus.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.jelly.zzirit.domain.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.repository.order.OrderRepository;

import lombok.RequiredArgsConstructor;

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

    /**
     * CANCELLED, COMPLETED, PAID 상태인 주문 내역을 조회하며 페이징 및 정렬 처리
     * @param memberId 현재 로그인한 유저의 아이디
     * @param pageable 페이징 및 정렬 정보
     * @return 페이징 및 정렬이 적용된 주문 리스트
     */
    public Page<Order> findPagedOrders(Long memberId, Pageable pageable) {
        EnumSet<OrderStatus> orderStatus = EnumSet.of(CANCELLED, COMPLETED, PAID);

        // Order가 페이징의 대상이므로, Order의 Id를 페이징 처리
        Page<Long> pagedIds = orderRepository.findOrderIdsByMemberIdAndStatuses(memberId, orderStatus, pageable);
        List<Long> ids = pagedIds.getContent();

        if (ids.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, pagedIds.getTotalElements());
        }

        // fetch join으로 관련된 엔티티(OrderItem, Item) 조회
        List<Order> orders = orderRepository.findByIdsWithItems(ids);

        Map<Long, Order> orderMap = orders.stream()
            .collect(Collectors.toMap(Order::getId, o -> o));

        // Order의 Id 순서 기준으로 재조립
        List<Order> sortedOrders = ids.stream()
            .map(orderMap::get)
            .filter(Objects::nonNull)
            .toList();

        return new PageImpl<>(sortedOrders, pageable, pagedIds.getTotalElements());
    }
}