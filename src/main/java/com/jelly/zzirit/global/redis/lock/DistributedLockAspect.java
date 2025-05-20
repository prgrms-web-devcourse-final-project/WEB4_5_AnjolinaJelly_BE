package com.jelly.zzirit.global.redis.lock;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {

	private static final String REDISSON_LOCK_PREFIX = "LOCK:";

	private final RedissonClient redissonClient;

	@Around("@annotation(DistributedLock)")
	public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

		String key = REDISSON_LOCK_PREFIX +
			CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
		RLock rLock = redissonClient.getLock(key);

		boolean available = rLock.tryLock(
			distributedLock.waitTime(),
			distributedLock.leaseTime(),
			distributedLock.timeUnit()
		);

		if (!available) {
			log.warn("락 획득 실패 - key: {}", key);
			return false;
		}

		try {
			Object result = joinPoint.proceed();

			if (TransactionSynchronizationManager.isSynchronizationActive()) {
				TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
					@Override
					public void afterCommit() {
						try {
							rLock.unlock();
							log.debug("트랜잭션 커밋 후 락 해제 - key: {}", key);
						} catch (Exception e) {
							log.error("락 해제 실패 - key: {}", key, e);
						}
					}
				});
			} else {
				rLock.unlock();
				log.debug("락 즉시 해제 (트랜잭션 없음) - key: {}", key);
			}

			return result;
		} catch (Throwable e) {
			rLock.unlock();
			log.debug("예외 발생으로 락 해제 - key: {}", key);
			throw e;
		}
	}
}