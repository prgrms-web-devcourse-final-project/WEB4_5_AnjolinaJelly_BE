package com.jelly.zzirit.global.security.util;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisRefreshTokenUtil {

	private final RedisTemplate<String, String> redisTemplate;

	public RedisRefreshTokenUtil(@Qualifier("TokenStringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void addRefreshToken(Long userId, String refreshToken, long ttl) {
		String key = generateRefreshKey(userId);
		redisTemplate.opsForValue().set(key, refreshToken, ttl, TimeUnit.MILLISECONDS);
	}

	public String getRefreshToken(Long userId) {
		String key = generateRefreshKey(userId);
		return redisTemplate.opsForValue().get(key);
	}

	public void deleteRefreshToken(Long userId) {
		String key = generateRefreshKey(userId);
		redisTemplate.delete(key);
	}

	private String generateRefreshKey(Long userId) {
		return AuthConst.TOKEN_REFRESH_REDIS_PREFIX + userId;
	}
}