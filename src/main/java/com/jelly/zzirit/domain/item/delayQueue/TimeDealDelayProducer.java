package com.jelly.zzirit.domain.item.delayQueue;

import static com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal.TimeDealStatus.*;

import java.util.concurrent.BlockingQueue;

import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeDealDelayProducer {

	private final BlockingQueue<TimeDealDelayTask> queue;

	public void register(TimeDeal timeDeal) {
		TimeDeal.TimeDealStatus status = timeDeal.getStatus();

		if (status == SCHEDULED) {
			log.info("타임딜 등록: id={}, 상태={}, 시작시간={}", timeDeal.getId(), status, timeDeal.getStartTime());
			queue.offer(new TimeDealDelayTask(timeDeal, timeDeal.getStartTime()));

			log.info("타임딜 등록: id={}, 상태={}, 종료시간={}", timeDeal.getId(), status, timeDeal.getEndTime());
			queue.offer(new TimeDealDelayTask(timeDeal, timeDeal.getEndTime()));

		} else if (status == ONGOING) {
			log.info("타임딜 등록: id={}, 상태={}, 종료시간={}", timeDeal.getId(), status, timeDeal.getEndTime());
			queue.offer(new TimeDealDelayTask(timeDeal, timeDeal.getEndTime()));
		}
	}
}
