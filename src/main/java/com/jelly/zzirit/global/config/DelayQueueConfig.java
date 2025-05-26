package com.jelly.zzirit.global.config;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jelly.zzirit.domain.item.delayQueue.TimeDealDelayTask;

@Configuration
public class DelayQueueConfig {
	@Bean
	public BlockingQueue<TimeDealDelayTask> timeDealDelayQueue() {
		return new DelayQueue<>();
	}
}
