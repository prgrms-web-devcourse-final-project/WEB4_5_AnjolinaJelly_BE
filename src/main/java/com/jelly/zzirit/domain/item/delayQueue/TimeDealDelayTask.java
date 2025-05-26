package com.jelly.zzirit.domain.item.delayQueue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class TimeDealDelayTask implements Delayed {

	private final TimeDeal timeDeal;
	private final long triggerTimeMillis;

	public TimeDealDelayTask(TimeDeal timeDeal, LocalDateTime executeAt) {
		this.timeDeal = timeDeal;
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
