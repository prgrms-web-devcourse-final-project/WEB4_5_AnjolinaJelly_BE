package com.jelly.zzirit.domain.order.scheduler;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static com.jelly.zzirit.domain.order.entity.Order.OrderStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusScheduler {

    private final OrderRepository orderRepository;

    @Scheduled(cron = "0 0 * * * *") // 매 정시마다 실행 // TODO: 정확히 24시간 경과 후 상태 변경을 보장하기 위한 고도화 필요
    public void markPaidOrdersAsCompleted() {
        LocalDateTime deadline = LocalDateTime.now().minusHours(24);

        // 24시간이 지나 COMPLETED 상태로 변경이 필요한 주문 조회
        List<Order> overdueOrders = orderRepository.findAllByStatusAndCreatedAtBefore(PAID, deadline);

        overdueOrders.forEach(order -> order.changeStatus(COMPLETED));

        log.info("총 {}개의 주문을 COMPLETED 상태로 변경했습니다.", overdueOrders.size());
    }

}
