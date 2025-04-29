package com.jelly.zzirit.global.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

	private final RedisTemplate<String, String> redisTemplate;

	public RedisService(@Qualifier("CachingStringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void setData(String key, String value, long timeoutSeconds) {
		redisTemplate.opsForValue().set(key, value, timeoutSeconds, TimeUnit.SECONDS);
	}

	public String getData(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	public void deleteData(String key) {
		redisTemplate.delete(key);
	}
}