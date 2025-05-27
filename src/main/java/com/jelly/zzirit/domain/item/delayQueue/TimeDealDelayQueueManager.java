package com.jelly.zzirit.domain.item.delayQueue;

import static com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal.TimeDealStatus.*;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;
import com.jelly.zzirit.domain.item.service.TimeDealSchedulerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeDealDelayQueueManager {

	private final TimeDealDelayProducer producer;
	private final TimeDealSchedulerService schedulerService;
	private final TimeDealRepository timeDealRepository;

	public void register(TimeDeal timeDeal) {
		producer.register(timeDeal);
	}

	public void execute(Long timeDealId) {
		// 타임딜 조회
		TimeDeal timeDeal = timeDealRepository.findById(timeDealId)
			.orElseThrow(() -> new IllegalArgumentException("타임딜을 찾을 수 없습니다. ID: " + timeDealId));

		LocalDateTime currentTime = LocalDateTime.now();
		LocalDateTime triggerTime = timeDeal.getStartTime();

		// 큐에서 꺼낸 직후 로깅
		long delayInSeconds = java.time.Duration.between(triggerTime, currentTime).toSeconds();
		log.info("큐에서 꺼낸 직후 - 타임딜 ID: {}, 현재 시간: {}, 트리거 시간: {}, 지연 시간(초): {}",
			timeDeal.getId(), currentTime, triggerTime, delayInSeconds);

		// 상태 변경을 위한 execute() 호출 직후 로깅
		log.info("상태 변경 직후 - 타임딜 ID: {}, 현재 시간: {}, 상태: {}",
			timeDeal.getId(), currentTime, timeDeal.getStatus());

		// 상태 변경
		if (timeDeal.getStatus() == SCHEDULED) {
			schedulerService.startScheduledDeal(timeDeal);
		} else if (timeDeal.getStatus() == ONGOING) {
			schedulerService.endOngoingDeal(timeDeal);
		}

		timeDealRepository.save(timeDeal);

		TimeDeal updatedTimeDeal = timeDealRepository.findById(timeDeal.getId())
			.orElseThrow(() -> new IllegalArgumentException("타임딜을 찾을 수 없습니다. ID: " + timeDeal.getId()));

		log.info("상태 변경 완료 - 타임딜 ID: {}, 현재 시간: {}, 변경된 상태: {}",
			updatedTimeDeal.getId(), currentTime, updatedTimeDeal.getStatus());
	}
}