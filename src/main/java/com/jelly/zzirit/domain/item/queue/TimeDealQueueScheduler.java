package com.jelly.zzirit.domain.item.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class TimeDealQueueScheduler {

    private final TimeDealQueueService timeDealQueueService;

    @Scheduled(cron = "0 0 0 1 * *") // 매월 1일 자정 실행
    public void scheduleTimeDealTasksForThisMonth() {
        YearMonth month = YearMonth.now();
        LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
        LocalDateTime startOfNextMonth = month.plusMonths(1).atDay(1).atStartOfDay();

        timeDealQueueService.enqueueTimeDealTasksInPeriod(startOfMonth, startOfNextMonth);
    }

}
