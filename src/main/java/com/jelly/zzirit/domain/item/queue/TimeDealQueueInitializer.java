package com.jelly.zzirit.domain.item.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Order(3) // DataInitializer(1) -> TimeDealDummyDataRunner(2) -> TimeDealQueueInitializer(3)
@Component
@RequiredArgsConstructor
public class TimeDealQueueInitializer implements ApplicationRunner {

    private final TimeDealQueueService timeDealQueueService;

    @Override
    public void run(ApplicationArguments args) {
        LocalDateTime now = LocalDateTime.now();
        YearMonth month = YearMonth.now();
        LocalDateTime startOfNextMonth = month.plusMonths(1).atDay(1).atStartOfDay();

        timeDealQueueService.enqueueTimeDealTasksInPeriod(now, startOfNextMonth);
    }

}
