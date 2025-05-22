package com.jelly.zzirit.domain.item.service;

import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal.TimeDealStatus;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeDealSchedulerService {

    private final TimeDealRepository timeDealRepository;
    private final CommandItemService commandItemService;

    // 시작 시간이 현재보다 이전인 SCHEDULED 상태 타임딜을 ONGOING 상태로 변경
    @Transactional
    public boolean startScheduledDeals(LocalDateTime now) {
        log.info("🔔 SCHEDULED → ONGOING 상태 변경할 타임딜 조회 시작: {}", now);
        TimeDeal toStartDeal = timeDealRepository.findTopByStatusOrderByStartTimeAsc(TimeDealStatus.SCHEDULED);
        log.info("🔔 SCHEDULED → ONGOING 상태 변경할 타임딜 조회 완료: {}", now);

        if (toStartDeal != null) {
            toStartDeal.updateStatus(TimeDealStatus.ONGOING);
            commandItemService.updateItemStatusByTimeDeal(toStartDeal, ItemStatus.TIME_DEAL);
            log.info("🔔 SCHEDULED → ONGOING 상태 변경 완료: {}", now);
            return true;
        }
        return false;
    }

    // 종료 시간이 현재보다 이전인 ONGOING 상태 타임딜을 ENDED 상태로 변경
    @Transactional
    public boolean endOngoingDeals(LocalDateTime now) {
        log.info("🔔 ONGOING → ENDED 상태 변경할 타임딜 조회 완료: {}", now);
        TimeDeal toEndDeal = timeDealRepository.findByStatusAndEndTimeBefore(TimeDealStatus.ONGOING,
                now);
        log.info("🔔 ONGOING → ENDED 상태 변경할 타임딜 조회 완료: {}", now);
        if (toEndDeal != null) {
            toEndDeal.updateStatus(TimeDealStatus.ENDED);
            commandItemService.updateItemStatusByTimeDeal(toEndDeal, ItemStatus.NONE);
            log.info("🔔 ONGOING → ENDED 상태 변경 완료: {}", now);
            return true;
        }
        return false;
    }
}