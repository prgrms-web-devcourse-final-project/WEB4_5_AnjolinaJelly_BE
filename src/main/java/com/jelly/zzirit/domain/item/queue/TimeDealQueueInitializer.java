package com.jelly.zzirit.domain.item.queue;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeDealQueueInitializer {

	private final TimeDealRepository timeDealRepository;
	private final TimeDealTaskProducer timeDealTaskProducer;

	@PostConstruct
	public void init() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);

		List<TimeDeal> todayRemainingDeals = timeDealRepository.findByStartOrEndBetween(now, endOfDay);
		log.info("서버 재시작: 오늘 남은 타임딜 {}개 큐 등록 시작 ({} ~ {})", todayRemainingDeals.size(), now, endOfDay);

		for (TimeDeal timeDeal : todayRemainingDeals) {
			try {
				timeDealTaskProducer.enqueueTimeDealTasks(timeDeal);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.error("큐 초기화 중 인터럽트 발생: 타임딜 ID={}", timeDeal.getId(), e);
			} catch (Exception e) {
				log.error("큐 초기화 중 예외 발생: 타임딜 ID={}", timeDeal.getId(), e);
			}
		}
	}
}