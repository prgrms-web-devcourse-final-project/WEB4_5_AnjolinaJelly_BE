package com.jelly.zzirit.domain.item.service;

import java.time.LocalDateTime;
import java.util.List;

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
        List<TimeDeal> toStartDeals = timeDealRepository.findByStatusAndStartTimeLessThanEqual(
                TimeDealStatus.SCHEDULED, now);
        log.info("🔔 SCHEDULED → ONGOING 상태 변경할 타임딜 조회 완료: {}", now);
        if (!toStartDeals.isEmpty()) {
            toStartDeals.forEach(deal -> deal.updateStatus(TimeDealStatus.ONGOING));
            toStartDeals.forEach(deal -> commandItemService.updateItemStatusByTimeDeal(deal, ItemStatus.TIME_DEAL));
            log.info("🔔 SCHEDULED → ONGOING 상태 변경 완료: {}", now);
            return true;
        }
        return false;
    }

    // 종료 시간이 현재보다 이전인 ONGOING 상태 타임딜을 ENDED 상태로 변경
    @Transactional
    public boolean endOngoingDeals(LocalDateTime now) {
        log.info("🔔 ONGOING → ENDED 상태 변경할 타임딜 조회 완료: {}", now);
        List<TimeDeal> toEndDeals = timeDealRepository.findByStatusAndEndTimeBefore(TimeDealStatus.ONGOING,
                now);
        log.info("🔔 ONGOING → ENDED 상태 변경할 타임딜 조회 완료: {}", now);
        if (!toEndDeals.isEmpty()) {
            toEndDeals.forEach(deal -> deal.updateStatus(TimeDealStatus.ENDED));
            toEndDeals.forEach(deal -> commandItemService.updateItemStatusByTimeDeal(deal, ItemStatus.NONE));
            log.info("🔔 ONGOING → ENDED 상태 변경 완료: {}", now);
            return true;
        }
        return false;
    }
}