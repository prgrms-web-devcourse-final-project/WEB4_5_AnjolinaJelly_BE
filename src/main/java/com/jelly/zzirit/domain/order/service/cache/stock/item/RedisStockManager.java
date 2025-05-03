package com.jelly.zzirit.domain.order.service.cache.stock.item;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.order.service.cache.stock.RedisStockKey;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisStockManager {

	private final RedissonClient redissonClient;
	private final TimeDealItemRepository timeDealItemRepository;

	public void decreaseItemStockOrThrow(Long itemId, int quantity) {
		String key = RedisStockKey.ITEM.of(itemId);
		decreaseStockOrThrow(key, quantity);
	}

	public void decreaseTimeDealStockOrThrow(Long timeDealItemId, int quantity) {
		TimeDealItem timeDealItem = timeDealItemRepository.findWithDeal(timeDealItemId)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ITEM_NOT_FOUND));

		if (!timeDealItem.getTimeDeal().isActiveNow()) {
			throw new InvalidOrderException(BaseResponseStatus.TIME_DEAL_NOT_ACTIVE);
		} // 타임딜 조건이 유효할 때만 결제 처리를 계속 진행

		String key = RedisStockKey.TIME_DEAL.of(timeDealItemId);
		decreaseStockOrThrow(key, quantity);
	}

	private void decreaseStockOrThrow(String redisKey, int quantity) {
		RBucket<Integer> bucket = redissonClient.getBucket(redisKey);
		Integer current = bucket.get();

		if (current == null) {
			throw new InvalidOrderException(BaseResponseStatus.ITEM_NOT_FOUND);
		}
		if (current < quantity) {
			throw new InvalidOrderException(BaseResponseStatus.OUT_OF_STOCK);
		}

		bucket.set(current - quantity);
	}

	public void restoreStock(Long targetId, int quantity, boolean isTimeDeal) {
		String key = isTimeDeal ? RedisStockKey.TIME_DEAL.of(targetId) : RedisStockKey.ITEM.of(targetId);
		RBucket<Integer> bucket = redissonClient.getBucket(key);

		Integer current = bucket.get();
		if (current == null) {
			bucket.set(quantity);
		} else {
			bucket.set(current + quantity);
		}
	}
}