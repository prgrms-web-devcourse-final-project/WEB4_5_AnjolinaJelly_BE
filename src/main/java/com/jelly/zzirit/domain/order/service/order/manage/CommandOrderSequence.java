package com.jelly.zzirit.domain.order.service.order.manage;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommandOrderSequence {

	private final StringRedisTemplate redisTemplate;
	private static final String ORDER_SEQ_KEY_PREFIX = "order:seq:";

	public Long getTodaySequence() {
		String key = getTodayKey();

		Long seq = redisTemplate.opsForValue().increment(key);
		if (seq == 1L) {
			redisTemplate.expire(key, Duration.ofDays(1));
		}
		return seq;
	}

	private String getTodayKey() {
		String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
		return ORDER_SEQ_KEY_PREFIX + today;
	}
}