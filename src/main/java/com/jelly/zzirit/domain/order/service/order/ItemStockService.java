package com.jelly.zzirit.domain.order.service.order;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemStockService {

	private final RedissonClient redissonClient;
	private final ItemStockRepository itemStockRepository;

	public void decrease(Long itemId, int quantity) {
		String lockKey = "lock:itemStock:" + itemId;
		RLock lock = redissonClient.getLock(lockKey);

		try {
			boolean isLocked = lock.tryLock(5, 3, TimeUnit.SECONDS);
			if (!isLocked) {
				throw new InvalidOrderException(BaseResponseStatus.LOCK_FAILED);
			}

			withLockDecrease(itemId, quantity);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new InvalidOrderException(BaseResponseStatus.LOCK_INTERRUPTED);
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	private void withLockDecrease(Long itemId, int quantity) {
		ItemStock stock = itemStockRepository.findByItemId(itemId)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.STOCK_NOT_FOUND));

		int remaining = stock.getQuantity() - stock.getSoldQuantity();
		if (remaining < quantity) {
			throw new InvalidOrderException(BaseResponseStatus.STOCK_REDUCE_FAILED);
		}

		stock.addSoldQuantity(quantity);
	}
}