package com.jelly.zzirit.global.redis.lock;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {

	private static final String REDISSON_LOCK_PREFIX = "LOCK:";

	private final RedissonClient redissonClient;
	private final AopForTransaction aopForTransaction;

	@Around("@annotation(DistributedLock)")
	public Object lock(ProceedingJoinPoint joinPoint) throws Throwable  {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

		String key = REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
		RLock rLock = redissonClient.getLock(key);

		try {
			boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
			if (!available) {
				return false;
			}

			return aopForTransaction.proceed(joinPoint);
		} catch (InterruptedException e) {
			throw new InterruptedException();
		} finally {
			try {
				rLock.unlock();
			} catch (IllegalMonitorStateException e) {
				log.info("Redisson Lock Already UnLock serviceName : {}, key : {}", method.getName(), key);
			}
		}
	}
}

// DistributedLock 어노테이션이 붙은 메서드를 호출할 때, 분산락을 획득/해제하는 부가적인 작업을 할 수 있도록 하는 Aspect