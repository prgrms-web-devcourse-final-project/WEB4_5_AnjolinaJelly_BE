package com.jelly.zzirit.domain.item.delayQueue;

import java.util.concurrent.BlockingQueue;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeDealDelayConsumer {

	private final TimeDealDelayQueueManager timeDealDelayQueueManager;
	private final BlockingQueue<TimeDealDelayTask> queue;

	@PostConstruct
	public void start() {
		Thread thread = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					TimeDealDelayTask task = queue.take(); // Blocking
					log.info("✅ 타임딜 실행 준비: id={}, 실행시간={}", task.getTimeDealId(), task.getTriggerTimeMillis());

					timeDealDelayQueueManager.execute(task.getTimeDealId());

				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					log.error("타임딜 실행 중 예외 발생", e);
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
}