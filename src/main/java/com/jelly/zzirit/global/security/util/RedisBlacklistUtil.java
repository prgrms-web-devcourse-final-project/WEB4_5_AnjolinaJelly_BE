package com.jelly.zzirit.global.security.util;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisBlacklistUtil {

	private final RedisTemplate<String, String> redisTemplate;

	public RedisBlacklistUtil(@Qualifier("TokenStringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void addToBlacklist(String token, long ttl) {
		String key = generateBlacklistKey(token);
		redisTemplate.opsForValue().set(key, "true", ttl, TimeUnit.MILLISECONDS);
	}

	public boolean isInBlackList(String token) {
		String key = generateBlacklistKey(token);
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}

	private String generateBlacklistKey(String token) {
		return AuthConst.TOKEN_BLACKLIST_PREFIX + token;
	}
}