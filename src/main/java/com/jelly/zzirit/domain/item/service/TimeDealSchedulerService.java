package com.jelly.zzirit.domain.item.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal.TimeDealStatus;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeDealSchedulerService {

	private final TimeDealRepository timeDealRepository;
	private final CommandItemService commandItemService;

	// 시작 시간이 현재보다 이전인 SCHEDULED 상태 타임딜을 ONGOING 상태로 변경
	@Transactional
	public boolean startScheduledDeals(LocalDateTime now) {
		TimeDeal toStartDeal = timeDealRepository.findByStatusAndEndTimeBefore(
			TimeDealStatus.SCHEDULED, now);
		if (toStartDeal != null) {
			toStartDeal.updateStatus(TimeDealStatus.ONGOING);
			commandItemService.updateItemStatusByTimeDeal(toStartDeal, ItemStatus.TIME_DEAL);
			return true;
		}
		return false;
	}

	// 종료 시간이 현재보다 이전인 ONGOING 상태 타임딜을 ENDED 상태로 변경
	@Transactional
	public boolean endOngoingDeals(LocalDateTime now) {
		TimeDeal toEndDeal = timeDealRepository.findByStatusAndEndTimeBefore(TimeDealStatus.ONGOING,
			now);
		if (toEndDeal != null) {
			toEndDeal.updateStatus(TimeDealStatus.ENDED);
			commandItemService.updateItemStatusByTimeDeal(toEndDeal, ItemStatus.NONE);
			return true;
		}
		return false;
	}
}
