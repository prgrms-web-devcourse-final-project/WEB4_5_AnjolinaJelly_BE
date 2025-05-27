package com.jelly.zzirit.domain.item.delayQueue;

import static com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal.TimeDealStatus.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

	public void execute(TimeDealDelayTask timeDealDelayTask) {

		LocalDateTime currentTime = LocalDateTime.now();
		Instant currentInstant = currentTime.atZone(ZoneId.systemDefault()).toInstant();
		Instant triggerInstant = Instant.ofEpochMilli(timeDealDelayTask.getTriggerTimeMillis());

		long delayInSeconds = Duration.between(triggerInstant, currentInstant).toSeconds();

		log.info("execute() 호출 - 타임딜 ID: {}, 현재 시간: {}, 트리거 시간: {}, 지연 시간(초): {}",
			timeDealDelayTask.getTimeDealId(), LocalDateTime.now(), timeDealDelayTask.getTriggerTimeMillis(),
			delayInSeconds);

		// 타임딜 조회
		TimeDeal timeDeal = timeDealRepository.findById(timeDealDelayTask.getTimeDealId())
			.orElseThrow(
				() -> new IllegalArgumentException("타임딜을 찾을 수 없습니다. ID: " + timeDealDelayTask.getTimeDealId()));

		// 상태 변경
		if (timeDeal.getStatus() == SCHEDULED) {
			schedulerService.startScheduledDeal(timeDeal);
		} else if (timeDeal.getStatus() == ONGOING) {
			schedulerService.endOngoingDeal(timeDeal);
		}

		TimeDeal updatedTimeDeal = timeDealRepository.findById(timeDeal.getId())
			.orElseThrow(() -> new IllegalArgumentException("타임딜을 찾을 수 없습니다. ID: " + timeDeal.getId()));

		// 타임딜이 실행된 후 경과 시간
		long elapsedTimeMillis = Duration.between(triggerInstant, Instant.now()).toMillis();
		log.info("타임딜 트리거 타임과 실행 완료 시간 차이: {}", elapsedTimeMillis);
	}
}