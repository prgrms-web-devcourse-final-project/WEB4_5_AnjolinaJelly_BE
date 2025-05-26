package com.jelly.zzirit.domain.item.delayQueue;

import static com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal.TimeDealStatus.*;

import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.service.TimeDealSchedulerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeDealDelayQueueManager {
	private final TimeDealDelayProducer producer;
	private final TimeDealSchedulerService schedulerService;

	public void register(TimeDeal timeDeal) {
		producer.register(timeDeal);
	}

	public void execute(TimeDeal timeDeal) {
		log.info("DelayTask 실행 - 타임딜 ID: {}, 상태: {}, 시작시간: {}, 종료시간: {}",
			timeDeal.getId(), timeDeal.getStatus(), timeDeal.getStartTime(), timeDeal.getEndTime());

		log.info("DelayTask 실행 전 상태 확인 - 타임딜 ID: {}, 현재 상태: {}", timeDeal.getId(), timeDeal.getStatus());

		if (timeDeal.getStatus() == SCHEDULED) {
			schedulerService.startScheduledDeals(timeDeal.getStartTime());
		} else if (timeDeal.getStatus() == ONGOING) {
			schedulerService.endOngoingDeals(timeDeal.getEndTime());
		}

		log.info("DelayTask 실행 후 상태 확인 - 타임딜 ID: {}, 변경된 상태: {}", timeDeal.getId(), timeDeal.getStatus());
	}
}