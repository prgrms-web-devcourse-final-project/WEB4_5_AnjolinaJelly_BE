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

    private void process(DelayedTimeDeal event){
        TimeDeal deal = timeDealRepository.findById(event.getTimeDealId())
                .orElse(null);
        if(deal == null) return;

        if(event.getIsStarting()){
            deal.updateStatus(TimeDealStatus.ONGOING);
            commandItemService.updateItemStatusByTimeDeal(deal, ItemStatus.TIME_DEAL);
            log.info("✅ 타임딜 시작됨: {}", deal.getId());
        } else {
            deal.updateStatus(TimeDealStatus.ENDED);
            commandItemService.updateItemStatusByTimeDeal(deal, ItemStatus.NONE);
            log.info("⛔ 타임딜 종료됨: {}", deal.getId());
        }
    }
}
