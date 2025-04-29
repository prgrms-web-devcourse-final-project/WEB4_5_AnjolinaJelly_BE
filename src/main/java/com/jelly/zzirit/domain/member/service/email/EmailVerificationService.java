package com.jelly.zzirit.domain.member.service.email;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.global.redis.RedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

	private final RedisService redisService;
	private static final String REDIS_KEY_PREFIX = "emailAuth:";
	private static final long CODE_EXPIRE_SEC = 600;
	private static final long REQUEST_LOCK_SEC = 10;

	public boolean isRequestLocked(String email) {
		return "true".equals(redisService.getData(REDIS_KEY_PREFIX + email + ":requestLock"));
	}

	public boolean isAlreadyVerified(String email) {
		return "true".equals(redisService.getData(REDIS_KEY_PREFIX + email + ":verified"));
	}

	public void storeVerificationCode(String email, String code) {
		log.info("이메일 인증 코드를 저장합니다: {}", email);

		// Redis 에 이메일 인증 코드를 저장 (600초 동안 유효)
		redisService.setData(REDIS_KEY_PREFIX + email + ":code", code, CODE_EXPIRE_SEC);
		log.info("Redis 에 인증 코드를 저장했습니다: {}", code);

		// 인증 여부 초기화 (false 로 설정, 600초 동안 유효)
		redisService.setData(REDIS_KEY_PREFIX + email + ":verified", "false", CODE_EXPIRE_SEC);
		log.info("Redis 에 이메일 인증 여부를 초기화했습니다: {}", email);

		// 이메일 요청 재발송 제한 잠금 설정 (10초 동안 잠금)
		redisService.setData(REDIS_KEY_PREFIX + email + ":requestLock", "true", REQUEST_LOCK_SEC);
		log.info("Redis 에 이메일 요청 잠금을 설정했습니다: {}", email);
	}


	public String getStoredCode(String email) {
		return redisService.getData(REDIS_KEY_PREFIX + email + ":code");
	}

	public void markAsVerified(String email) {
		redisService.setData(REDIS_KEY_PREFIX + email + ":verified", "true", CODE_EXPIRE_SEC);
		redisService.deleteData(REDIS_KEY_PREFIX + email + ":code");
	}

	public void clearVerificationCode(String email) {
		redisService.deleteData(REDIS_KEY_PREFIX + email + ":code");
	}
}