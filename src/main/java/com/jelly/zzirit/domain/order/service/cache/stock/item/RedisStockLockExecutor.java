package com.jelly.zzirit.domain.order.service.cache.stock.item;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStockLockExecutor {

	private final RedissonClient redissonClient;

	public void executeWithLock(String lockKey, Runnable task, boolean isTimeDeal) {
		if (isTimeDeal) {
			executeLockOnce(lockKey, task, 300, 1000);
		} else {
			executeLockWithRetry(lockKey, task);
		}
	}

	private void executeLockOnce(String lockKey, Runnable task, long waitTime, long leaseTime) {
		RLock lock = redissonClient.getLock(lockKey);
		try {
			boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
			if (!acquired) {
				throw new InvalidOrderException(BaseResponseStatus.LOCK_FAILED);
			}
			task.run();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new InvalidOrderException(BaseResponseStatus.LOCK_INTERRUPTED);
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	@Retryable(
		backoff = @Backoff(delay = 200, multiplier = 2)
	)
	public void executeLockWithRetry(String lockKey, Runnable task) {
		executeLockOnce(lockKey, task, 3000, 5000);
	}

	@Recover
	public void recover(InvalidOrderException e, String lockKey) {
		log.warn("락 재시도 실패: key={}, message={}", lockKey, e.getMessage());
		throw new InvalidOrderException(BaseResponseStatus.LOCK_FAILED);
	}
}