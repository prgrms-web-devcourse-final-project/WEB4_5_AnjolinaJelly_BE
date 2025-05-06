package com.jelly.zzirit.domain.order.service.order;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderSequenceGenerator {

	private final StringRedisTemplate cachingStringRedisTemplate;

	private static final String ORDER_SEQ_KEY_PREFIX = "order:seq:";

	public Long getTodaySequence() {
		String key = getTodayKey();
		cachingStringRedisTemplate.opsForValue().setIfAbsent(key, "0", Duration.ofDays(1));
		return cachingStringRedisTemplate.opsForValue().increment(key);
	}

	private String getTodayKey() {
		String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
		return ORDER_SEQ_KEY_PREFIX + today;
	}
}