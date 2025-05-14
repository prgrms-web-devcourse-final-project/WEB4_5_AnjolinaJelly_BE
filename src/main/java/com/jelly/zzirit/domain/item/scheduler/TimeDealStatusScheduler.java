package com.jelly.zzirit.domain.item.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.item.service.TimeDealSchedulerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeDealStatusScheduler {

	private final TimeDealSchedulerService timeDealSchedulerService;

	@Scheduled(fixedRate = 60_000) // 1분마다 실행
	public void updateTimeDealStatuses() {
		LocalDateTime now = LocalDateTime.now();

		int toStartDeals = timeDealSchedulerService.convertTimeDealStatusScheduledToOngoing(now);
		int toEndDeals = timeDealSchedulerService.convertTimeDealStatusOngoingToEnded(now);

		log.info("시작된 타임딜: {}개, 종료된 타임딜: {}개", toStartDeals, toEndDeals);
	}
}
