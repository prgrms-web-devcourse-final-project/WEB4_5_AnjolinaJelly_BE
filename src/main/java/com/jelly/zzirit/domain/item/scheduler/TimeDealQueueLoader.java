package com.jelly.zzirit.domain.item.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.item.service.TimeDealSchedulerService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TimeDealQueueLoader {
	private final TimeDealSchedulerService schedulerService;

	@Scheduled(cron = "0 0 0 * * ?") // 매일 0시 실행
	public void loadUpcomingDeals() {
		schedulerService.loadUpcomingDealsToQueue();
	}
}
