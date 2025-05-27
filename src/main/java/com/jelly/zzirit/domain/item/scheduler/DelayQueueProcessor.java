package com.jelly.zzirit.domain.item.scheduler;

import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal.TimeDealStatus;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;
import com.jelly.zzirit.domain.item.service.CommandItemService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

@Slf4j
@RequiredArgsConstructor
@Component
public class DelayQueueProcessor {
    private final BlockingQueue<DelayedTimeDeal> queue = new DelayQueue<>();
    private final TimeDealRepository timeDealRepository;
    private final CommandItemService commandItemService;

    @PostConstruct
    public void restorePendingDeals() {
        List<TimeDeal> pendingDeals = timeDealRepository.findAllByStatusIn(List.of(
                TimeDealStatus.SCHEDULED,
                TimeDealStatus.ONGOING
        ));

        LocalDateTime now = LocalDateTime.now();

        for (TimeDeal deal : pendingDeals) {
            LocalDateTime start = deal.getStartTime();
            LocalDateTime end = deal.getEndTime();

            if (start.isAfter(now)) {
                // ⏳ 시작 시간이 아직 안 됨 → 시작/종료 둘 다 등록
                queue.add(new DelayedTimeDeal(deal, true));
                queue.add(new DelayedTimeDeal(deal, false));
                log.info("🔄 타임딜 복구: 아직 시작 전 → id({}) 시작/종료 등록", deal.getId());

            } else if (start.isBefore(now) && end.isAfter(now)) {
                // ▶️ 이미 시작했지만 아직 종료 전 → 종료만 등록
                queue.add(new DelayedTimeDeal(deal, false));
                log.info("🔄 타임딜 복구: 진행 중 → id({}) 종료만 등록", deal.getId());

            } else {
                // ✅ 이미 종료됨 → 아무것도 안 함
                log.info("⏹️ 타임딜 복구 스킵: 이미 종료됨 → id({})", deal.getId());
            }
        }

        log.info("✅ DelayQueue 복구 완료. 총 복구된 타임딜 수: {}", pendingDeals.size());
    }

    @PostConstruct
    public void init(){
        Thread thread = new Thread(() -> {
            while(true){
                try{
                    DelayedTimeDeal event = queue.take();
                    process(event);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void schedule(TimeDeal timeDeal){
        try{
            queue.put(new DelayedTimeDeal(timeDeal, true));
            queue.put(new DelayedTimeDeal(timeDeal, false));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Delay queue processing interrupted");
        }
    }

    @Transactional
    protected void process(DelayedTimeDeal event){
        TimeDeal deal = timeDealRepository.findById(event.getTimeDealId())
                .orElse(null);
        if(deal == null) return;

        if(event.getIsStarting()){
            deal.updateStatus(TimeDealStatus.ONGOING);
            commandItemService.updateItemStatusByTimeDeal(deal, ItemStatus.TIME_DEAL);
            log.info("✅ 타임딜 시작됨: id({}), 예약시간({}), 현재시각({}), 지연시간({})", deal.getId(), event.getExpTime(), System.currentTimeMillis(), System.currentTimeMillis() - event.getExpTime());
        } else {
            deal.updateStatus(TimeDealStatus.ENDED);
            commandItemService.updateItemStatusByTimeDeal(deal, ItemStatus.NONE);
            log.info("⛔ 타임딜 종료됨: id({}), 예약시간({}), 현재시각({}), 지연시간({})", deal.getId(), event.getExpTime(), System.currentTimeMillis(), System.currentTimeMillis() - event.getExpTime());
        }
    }
}
