package com.jelly.zzirit.domain.item.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.delayQueue.TimeDealDelayTask;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal.TimeDealStatus;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeDealSchedulerService {

	private final TimeDealRepository timeDealRepository;
	private final CommandItemService commandItemService;
	private final BlockingQueue<TimeDealDelayTask> timeDealDelayQueue;

	// 기존 메서드는 그대로 두고, 개별 처리용 메서드 추가!
	@Transactional
	public void startScheduledDeal(TimeDeal timeDeal) {
		if (timeDeal.getStatus() == TimeDealStatus.SCHEDULED) {
			timeDeal.updateStatus(TimeDealStatus.ONGOING);
			commandItemService.updateItemStatusByTimeDeal(timeDeal, ItemStatus.TIME_DEAL);
			log.info("🔔 SCHEDULED → ONGOING 상태 변경 완료 - 타임딜 ID: {}", timeDeal.getId());
		} else {
			log.warn("SCHEDULED → ONGOING 상태 변경 실패 - 타임딜 상태 불일치. 현재 상태: {}", timeDeal.getStatus());
		}
	}

	@Transactional
	public void endOngoingDeal(TimeDeal timeDeal) {
		if (timeDeal.getStatus() == TimeDealStatus.ONGOING) {
			timeDeal.updateStatus(TimeDealStatus.ENDED);
			commandItemService.updateItemStatusByTimeDeal(timeDeal, ItemStatus.NONE);
			log.info("🔔 ONGOING → ENDED 상태 변경 완료 - 타임딜 ID: {}", timeDeal.getId());
		} else {
			log.warn("ONGOING → ENDED 상태 변경 실패 - 타임딜 상태 불일치. 현재 상태: {}", timeDeal.getStatus());
		}
	}

	// 시작 시간이 현재보다 이전인 SCHEDULED 상태 타임딜을 ONGOING 상태로 변경
	@Transactional
	public boolean startScheduledDeals(LocalDateTime now) {
		log.info("🔔 SCHEDULED → ONGOING 상태 변경할 타임딜 조회 시작: {}", now);
		List<TimeDeal> toStartDeals = timeDealRepository.findByStatusAndStartTimeLessThanEqual(
			TimeDealStatus.SCHEDULED, now);
		log.info("🔔 SCHEDULED → ONGOING 상태 변경할 타임딜 조회 완료: {}", now);
		if (!toStartDeals.isEmpty()) {
			toStartDeals.forEach(deal -> deal.updateStatus(TimeDealStatus.ONGOING));
			toStartDeals.forEach(deal -> commandItemService.updateItemStatusByTimeDeal(deal, ItemStatus.TIME_DEAL));
			log.info("🔔 SCHEDULED → ONGOING 상태 변경 완료: {}", now);
			return true;
		}
		return false;
	}

	// 종료 시간이 현재보다 이전인 ONGOING 상태 타임딜을 ENDED 상태로 변경
	@Transactional
	public boolean endOngoingDeals(LocalDateTime now) {
		log.info("🔔 ONGOING → ENDED 상태 변경할 타임딜 조회 완료: {}", now);
		List<TimeDeal> toEndDeals = timeDealRepository.findByStatusAndEndTimeBefore(TimeDealStatus.ONGOING,
			now);
		log.info("🔔 ONGOING → ENDED 상태 변경할 타임딜 조회 완료: {}", now);
		if (!toEndDeals.isEmpty()) {
			toEndDeals.forEach(deal -> deal.updateStatus(TimeDealStatus.ENDED));
			toEndDeals.forEach(deal -> commandItemService.updateItemStatusByTimeDeal(deal, ItemStatus.NONE));
			log.info("🔔 ONGOING → ENDED 상태 변경 완료: {}", now);
			return true;
		}
		return false;
	}

	@Transactional
	public void loadUpcomingDealsToQueue() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime weekLater = now.plusWeeks(1);

		List<TimeDeal> deals = timeDealRepository.findUpcomingScheduledDeals(now, weekLater);

		for (TimeDeal deal : deals) {
			// 중복된 타임딜이 큐에 있는지 확인
			if (!isTimeDealInQueue(deal)) {
				TimeDealDelayTask startTask = new TimeDealDelayTask(deal.getId(), deal.getStartTime());
				TimeDealDelayTask endTask = new TimeDealDelayTask(deal.getId(), deal.getEndTime());

				timeDealDelayQueue.offer(startTask);
				timeDealDelayQueue.offer(endTask);

				log.info("타임딜 큐 등록 - 타임딜 ID: {}, 시작시간: {}, 종료시간: {}", deal.getId(), deal.getStartTime(),
					deal.getEndTime());
			}
		}
	}

	// 큐에 타임딜이 이미 있는지 확인하는 메서드
	private boolean isTimeDealInQueue(TimeDeal deal) {
		// 큐의 작업 중 타임딜 ID가 동일한 것이 있는지 확인
		return timeDealDelayQueue.stream()
			.anyMatch(task -> task.getTimeDealId() == (deal.getId()));
	}
}