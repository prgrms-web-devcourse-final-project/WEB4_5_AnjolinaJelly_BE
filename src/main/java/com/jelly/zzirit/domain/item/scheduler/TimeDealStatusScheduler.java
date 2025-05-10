package com.jelly.zzirit.domain.item.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeDealStatusScheduler {

	private final TimeDealRepository timeDealRepository;

	@Scheduled(fixedRate = 60_000) // 1분마다 실행
	public void updateTimeDealStatuses() {
		LocalDateTime now = LocalDateTime.now();

		// 시작 시간이 지났지만 아직 시작되지 않은 타임딜 (SCHEDULED → ONGOING)
		List<TimeDeal> toStartDeals = timeDealRepository.findAllByStatusAndStartTimeLessThanEqual(
			TimeDeal.TimeDealStatus.SCHEDULED, now);
		toStartDeals.forEach(deal -> deal.updateStatus(TimeDeal.TimeDealStatus.ONGOING));

		// 종료 시간이 지난 타임딜 (ONGOING → ENDED)
		List<TimeDeal> toEndDeals = timeDealRepository.findAllByStatusAndEndTimeBefore(TimeDeal.TimeDealStatus.ONGOING,
			now);
		toEndDeals.forEach(deal -> deal.updateStatus(TimeDeal.TimeDealStatus.ENDED));

		log.info("시작된 타임딜: {}개, 종료된 타임딜: {}개", toStartDeals.size(), toEndDeals.size());
	}
}
