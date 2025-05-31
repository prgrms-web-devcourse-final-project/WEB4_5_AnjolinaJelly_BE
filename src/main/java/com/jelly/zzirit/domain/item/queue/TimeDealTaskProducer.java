package com.jelly.zzirit.domain.item.queue;

import static com.jelly.zzirit.domain.item.entity.ItemStatus.*;
import static com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal.TimeDealStatus.*;

import java.time.LocalDate;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.global.exception.custom.TimeDealQueueException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeDealTaskProducer {

	private final BlockingQueue<TimeDealTask> queue;

	public void produce(TimeDeal timeDeal) {
		try {
			enqueueTimeDealTasks(timeDeal); // 딜레이 큐에 상태 변경 작업 추가
		} catch (InterruptedException e) { // 스레드 종료 요청 수신
			Thread.currentThread().interrupt(); // 인터럽트 플래그 복원
			log.error("타임 딜 및 상품 상태 변경 작업 추가 중 인터럽트 발생: 타임 딜 아이디={}", timeDeal.getId(), e);
			throw new TimeDealQueueException("타임 딜 및 상품 상태 변경 작업 추가 중 예외 발생", e); // 트랜잭션 롤백 유도
		}
	}

	public void enqueueTimeDealTasks(TimeDeal timeDeal) throws InterruptedException {
		boolean taskQueued = false;
		LocalDate today = LocalDate.now();

		// 시작 Task 등록 (오늘 시작인 경우)
		if (timeDeal.getStartTime().toLocalDate().isEqual(today)) {
			TimeDealTask startTask = new TimeDealTask(
				timeDeal.getId(),
				ONGOING,
				TIME_DEAL,
				timeDeal.getStartTime()
			);
			taskQueued |= queue.offer(startTask, 3, TimeUnit.SECONDS);
			log.info("[Task 등록] 시작 Task 등록 완료: {}", startTask);
		}

		// 종료 Task 등록 (오늘 종료인 경우)
		if (timeDeal.getEndTime().toLocalDate().isEqual(today)) {
			TimeDealTask endTask = new TimeDealTask(
				timeDeal.getId(),
				ENDED,
				NONE,
				timeDeal.getEndTime()
			);
			taskQueued |= queue.offer(endTask, 3, TimeUnit.SECONDS);
			log.info("[Task 등록] 종료 Task 등록 완료: {}", endTask);
		}

		if (!taskQueued) {
			log.info("[Task 등록] 오늘 처리할 Task 없음: 타임딜 ID={}", timeDeal.getId());
		}
	}

}
