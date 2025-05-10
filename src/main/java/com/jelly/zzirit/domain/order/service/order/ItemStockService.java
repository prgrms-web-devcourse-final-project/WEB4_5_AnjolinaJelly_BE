package com.jelly.zzirit.domain.order.service.order;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import com.jelly.zzirit.global.redis.lock.DistributedLock;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemStockService {

	private final ItemStockRepository itemStockRepository;

	@DistributedLock(key = "#itemId")
	public void decrease(Long itemId, int quantity) {
		ItemStock stock = itemStockRepository.findByItemId(itemId)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.STOCK_NOT_FOUND));

		int remaining = stock.getQuantity() - stock.getSoldQuantity();
		if (remaining < quantity) {
			throw new InvalidOrderException(BaseResponseStatus.STOCK_REDUCE_FAILED);
		}

		stock.addSoldQuantity(quantity);
	}
}