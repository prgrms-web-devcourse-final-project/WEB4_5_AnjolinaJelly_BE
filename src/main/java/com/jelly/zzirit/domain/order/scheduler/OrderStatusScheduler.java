package com.jelly.zzirit.domain.order.scheduler;

import com.jelly.zzirit.domain.order.service.order.CommandOrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusScheduler {

    private final CommandOrderService commandOrderService;

    @Scheduled(cron = "0 */10 * * * *") // 매 10분마다 실행
    public void updatePaidOrdersToCompleted() {
        int updatedOrderCount = commandOrderService.completeExpiredOrders();
        log.info("총 {}개의 주문을 COMPLETED 상태로 변경했습니다.", updatedOrderCount);
    }

}
