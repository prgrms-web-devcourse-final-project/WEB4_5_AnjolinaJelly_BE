package com.jelly.zzirit.domain.item.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.queue.TimeDealTaskProducer;
import com.jelly.zzirit.domain.item.repository.TimeDealRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyTimeDealScheduler {

	private final TimeDealRepository timeDealRepository;
	private final TimeDealTaskProducer producer;

	@Scheduled(cron = "0 0 23 * * *")
	public void registerTomorrowTimeDealTasks() {
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		List<TimeDeal> tomorrowDeals = timeDealRepository.findByStartOrEndDateTomorrow(tomorrow);
		log.info("스케줄러: 내일 처리할 타임딜 {}개 큐 등록 시작", tomorrowDeals.size());

		tomorrowDeals.forEach(producer::produce);
	}
}
