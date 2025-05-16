package com.jelly.zzirit.domain.item.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeDealSchedulerService {
	private final TimeDealRepository timeDealRepository;

	// 시작 시간이 현재보다 이전인 SCHEDULED 상태 타임딜을 ONGOING 상태로 변경
	@Transactional
	public int startScheduledDeals(LocalDateTime now) {
		List<TimeDeal> toStartDeals = timeDealRepository.findAllByStatusAndStartTimeLessThanEqual(
			TimeDeal.TimeDealStatus.SCHEDULED, now);
		toStartDeals.forEach(deal -> deal.updateStatus(TimeDeal.TimeDealStatus.ONGOING));

		return toStartDeals.size();
	}

	// 종료 시간이 현재보다 이전인 ONGOING 상태 타임딜을 ENDED 상태로 변경
	@Transactional
	public int endOngoingDeals(LocalDateTime now) {
		List<TimeDeal> toEndDeals = timeDealRepository.findAllByStatusAndEndTimeBefore(TimeDeal.TimeDealStatus.ONGOING,
			now);
		toEndDeals.forEach(deal -> deal.updateStatus(TimeDeal.TimeDealStatus.ENDED));

		return toEndDeals.size();
	}
}
