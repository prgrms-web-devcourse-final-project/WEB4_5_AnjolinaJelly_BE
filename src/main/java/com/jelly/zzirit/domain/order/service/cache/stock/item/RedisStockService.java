package com.jelly.zzirit.domain.order.service.cache.stock.item;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.order.service.cache.stock.RedisStockKey;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisStockService {

	private final RedisStockManager redisStockManager;
	private final RedisStockLockExecutor lockExecutor;

	public void reserveStock(Long targetId, int quantity, boolean isTimeDeal) {
		String lockKey = isTimeDeal
			? RedisStockKey.LOCK_TIME_DEAL.of(targetId)
			: RedisStockKey.LOCK_ITEM.of(targetId);

		lockExecutor.executeWithLock(lockKey, () -> {
			if (isTimeDeal) {
				redisStockManager.decreaseTimeDealStockOrThrow(targetId, quantity);
			} else {
				redisStockManager.decreaseItemStockOrThrow(targetId, quantity);
			}
		}, isTimeDeal);
	}
}