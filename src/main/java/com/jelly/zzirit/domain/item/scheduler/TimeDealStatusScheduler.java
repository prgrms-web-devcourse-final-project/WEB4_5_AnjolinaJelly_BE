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
	public void markEndedTimeDeals() {
		LocalDateTime now = LocalDateTime.now();

		// 현재 상태가 ONGOING 이지만 종료 시간이 지난 타임딜 리스트 조회
		List<TimeDeal> endedDeals = timeDealRepository.findAllByStatusAndEndTimeBefore(TimeDeal.TimeDealStatus.ONGOING,
			now);

		endedDeals.forEach(deal -> deal.updateStatus(TimeDeal.TimeDealStatus.ENDED));

		log.info("총 {}개의 타임딜이 ENDED 상태로 변경되었습니다.", endedDeals.size());
	}
}
