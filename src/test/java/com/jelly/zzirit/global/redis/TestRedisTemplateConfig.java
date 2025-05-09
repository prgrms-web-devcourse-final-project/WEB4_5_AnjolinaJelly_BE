package com.jelly.zzirit.global.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

@TestConfiguration
public class TestRedisTemplateConfig {

	@Bean
	@Qualifier("CachingStringRedisTemplate")
	public RedisTemplate<String, String> cachingStringRedisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		redisTemplate.setConnectionFactory(connectionFactory);
		return redisTemplate;
	}

	@Bean
	@Qualifier("TokenStringRedisTemplate")
	public RedisTemplate<String, String> tokenStringRedisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		redisTemplate.setConnectionFactory(connectionFactory);
		return redisTemplate;
	}

	@Bean
	@Qualifier("TokenJsonRedisTemplate")
	public RedisTemplate<String, Object> tokenJsonRedisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper));
		redisTemplate.setConnectionFactory(connectionFactory);
		return redisTemplate;
	}
}