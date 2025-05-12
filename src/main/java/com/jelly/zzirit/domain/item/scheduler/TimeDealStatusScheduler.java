package com.jelly.zzirit.domain.item.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.item.service.TimeDealService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeDealStatusScheduler {

	private final TimeDealService timeDealService;

	@Scheduled(fixedRate = 60_000) // 1분마다 실행
	public void updateTimeDealStatuses() {
		LocalDateTime now = LocalDateTime.now();

		int toStartDeals = timeDealService.convertTimeDealStatusScheduledToOngoing(now);
		int toEndDeals = timeDealService.converTimeDealStatusOngoingToEnded(now);

		log.info("시작된 타임딜: {}개, 종료된 타임딜: {}개", toStartDeals, toEndDeals);
	}
}
