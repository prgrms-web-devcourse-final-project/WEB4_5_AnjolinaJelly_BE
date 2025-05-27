package com.jelly.zzirit.domain.item.delayQueue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class TimeDealDelayTask implements Delayed {

	private final long timeDealId;
	private final long triggerTimeMillis;

	// 타임딜 ID만 저장
	public TimeDealDelayTask(long timeDealId, LocalDateTime executeAt) {
		this.timeDealId = timeDealId;
		this.triggerTimeMillis = executeAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	@Override
	public long getDelay(TimeUnit unit) {
		long diff = triggerTimeMillis - System.currentTimeMillis();
		return unit.convert(diff, TimeUnit.MILLISECONDS);
	}

	@Override
	public int compareTo(Delayed other) {
		return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), other.getDelay(TimeUnit.MILLISECONDS));
	}
}