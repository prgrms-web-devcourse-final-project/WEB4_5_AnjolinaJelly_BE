package com.jelly.zzirit.domain.order.service.cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisStockService {

	private final StringRedisTemplate redisTemplate;

	public void reserveItemStock(Long itemId, int quantity) {
		String key = "stock:item:" + itemId;
		decreaseOrThrow(key, quantity);
	}

	public void reserveTimeDealStock(Long timeDealItemId, int quantity) {
		String key = "stock:timedeal:" + timeDealItemId;
		decreaseOrThrow(key, quantity);
	}

	private void decreaseOrThrow(String key, int quantity) {
		Long remaining = redisTemplate.opsForValue().increment(key, -quantity);

		if (remaining == null) {
			throw new InvalidOrderException(BaseResponseStatus.ITEM_NOT_FOUND);
		}

		if (remaining < 0) {
			redisTemplate.opsForValue().increment(key, quantity); // 롤백
			throw new InvalidOrderException(BaseResponseStatus.OUT_OF_STOCK);
		}
	}
} // 재고 수량을 선점(DECR