package com.jelly.zzirit.global.security.util;

import java.time.Duration;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidAuthenticationException;
import com.jelly.zzirit.global.exception.custom.InvalidRedisException;

@Component
public class AccountLoginRateUtil {

	private final RedisTemplate<String, String> redisTemplate;

	public AccountLoginRateUtil(@Qualifier("CachingStringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	// 차단 기준
	private static final int MAX_FAIL_ACCOUNT = 5;
	private static final int MAX_FAIL_IP = 10;
	private static final int MAX_RATE_IP_PER_MIN = 20;

	// 차단 지속 시간
	private static final Duration BLOCK_DURATION = Duration.ofMinutes(5);
	private static final Duration RATE_WINDOW = Duration.ofMinutes(1);

	private String keyFailAccount(String email) { return "login:fail:account:" + email; }
	// 계정 기준 실패 횟수 카운트 키

	private String keyBlockAccount(String email) { return "login:block:account:" + email; }
	// 계정 기준 차단 여부 키

	private String keyFailIp(String ip) { return "login:fail:ip:" + ip; }
	// IP 기준 실패 횟수 카운트 키

	private String keyBlockIp(String ip) { return "login:block:ip:" + ip; }
	// IP 기준 차단 여부 키

	private String keyRateIp(String ip) { return "login:rate:ip:" + ip; }
	// IP 기준 요청 속도 제한 카운트 키 (1분 단위)

	public void checkAccountLock(String email) {
		if (Boolean.TRUE.equals(redisTemplate.hasKey(keyBlockAccount(email)))) {
			throw new InvalidAuthenticationException(BaseResponseStatus.ACCOUNT_LOCKED);
		}
	} // 계정 차단 여부 검사

	public void checkIpRateLimit(String ip) {
		if (Boolean.TRUE.equals(redisTemplate.hasKey(keyBlockIp(ip)))) {
			throw new InvalidAuthenticationException(BaseResponseStatus.IP_BLOCKED);
		}

		long count = incrementOrThrow(keyRateIp(ip), RATE_WINDOW);
		if (count >= MAX_RATE_IP_PER_MIN) {
			redisTemplate.opsForValue().set(keyBlockIp(ip), "BLOCKED", BLOCK_DURATION);
			throw new InvalidAuthenticationException(BaseResponseStatus.IP_BLOCKED);
		}
	} //  IP 차단 여부 및 요청 속도 검사

	public void recordLoginFailure(String email, String ip) {
		long accCount = incrementOrThrow(keyFailAccount(email), BLOCK_DURATION);
		if (accCount >= MAX_FAIL_ACCOUNT) {
			redisTemplate.opsForValue().set(keyBlockAccount(email), "BLOCKED", BLOCK_DURATION);
		}

		long ipCount = incrementOrThrow(keyFailIp(ip), BLOCK_DURATION);
		if (ipCount >= MAX_FAIL_IP) {
			redisTemplate.opsForValue().set(keyBlockIp(ip), "BLOCKED", BLOCK_DURATION);
		}
	} // 로그인 실패 기록 (계정 + IP 기준)

	public void resetLoginFailures(String email, String ip) {
		redisTemplate.delete(Arrays.asList(
			keyFailAccount(email), keyBlockAccount(email),
			keyFailIp(ip), keyBlockIp(ip),
			keyRateIp(ip)
		));
	} // 성공 시 실패 기록 초기화

	private long incrementOrThrow(String key, Duration ttl) {
		Long count = redisTemplate.opsForValue().increment(key);
		if (count == null) {
			throw new InvalidRedisException(BaseResponseStatus.REDIS_ACCESS_ERROR);
		}
		if (count == 1L) {
			redisTemplate.expire(key, ttl);
		}
		return count;
	}
}