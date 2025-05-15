package com.jelly.zzirit.domain.order.service.order;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import com.jelly.zzirit.global.redis.lock.DistributedLock;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommandStockService {

	private final ItemStockRepository itemStockRepository;

	@DistributedLock(
		key = "#itemId",
		leaseTime = 12L
	)
	public void decrease(Long itemId, int quantity) {
		boolean success = itemStockRepository.decreaseStockIfEnough(itemId, quantity);

		if (!success) {
			throw new InvalidOrderException(BaseResponseStatus.STOCK_REDUCE_FAILED);
		}
	}

	@DistributedLock(
		key = "#itemId",
		leaseTime = 12L
	)
	public void restore(Long itemId, int quantity) {
		boolean success = itemStockRepository.restoreStockIfPossible(itemId, quantity);

		if (!success) {
			throw new InvalidOrderException(BaseResponseStatus.STOCK_RESTORE_FAILED);
		}
	}
}