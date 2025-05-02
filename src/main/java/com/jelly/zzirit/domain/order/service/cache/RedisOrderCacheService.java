package com.jelly.zzirit.domain.order.service.cache;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.order.dto.request.RedisOrderData;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisOrderCacheService {

	private final RedisTemplate<String, RedisOrderData> redisTemplate;

	private static final String PREFIX = "order:cache:";

	public void save(String orderId, RedisOrderData data, Duration ttl) {
		redisTemplate.opsForValue().set(PREFIX + orderId, data, ttl);
	}

	public Optional<RedisOrderData> get(String orderId) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(PREFIX + orderId));
	}

	public void remove(String orderId) {
		redisTemplate.delete(PREFIX + orderId);
	}
}