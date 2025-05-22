package com.jelly.zzirit.domain.order.service.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.domain.order.dto.StockChangeEvent;
import com.jelly.zzirit.domain.order.util.AsyncStockHistoryUploader;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import com.jelly.zzirit.global.redis.lock.DistributedLock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandStockService {

	private final ItemRepository itemRepository;
	private final TimeDealItemRepository timeDealItemRepository;
	private final ItemStockRepository itemStockRepository;
	private final AsyncStockHistoryUploader asyncStockHistoryUploader;

	@DistributedLock(key = "#itemId", leaseTime = 12L)
	@Transactional
	public void decrease(String orderNumber, Long itemId, int quantity) {
		Item item = itemRepository.findById(itemId)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ITEM_NOT_FOUND));

		boolean success = item.validateTimeDeal()
			? tryDecreaseTimeDealStock(itemId, quantity)
			: itemStockRepository.decreaseStockIfEnough(itemId, quantity);

		if (!success) {
			throw new InvalidOrderException(BaseResponseStatus.STOCK_REDUCE_FAILED);
		}

		registerLogAfterCommit(StockChangeEvent.decrease(itemId, orderNumber, quantity));
	}

	@Transactional
	@DistributedLock(key = "#itemId", leaseTime = 12L)
	public void restore(String orderNumber, Long itemId, int quantity) {
		Item item = itemRepository.findById(itemId)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ITEM_NOT_FOUND));

		boolean success = item.validateTimeDeal()
			? tryRestoreTimeDealStock(itemId, quantity)
			: itemStockRepository.restoreStockIfPossible(itemId, quantity);

		if (!success) {
			throw new InvalidOrderException(BaseResponseStatus.STOCK_RESTORE_FAILED);
		}

		registerLogAfterCommit(StockChangeEvent.restore(itemId, orderNumber, quantity));
	}

	private boolean tryDecreaseTimeDealStock(Long itemId, int quantity) {
		return timeDealItemRepository.findByItemId(itemId)
			.map(tdi -> itemStockRepository.decreaseTimeDealStockIfEnough(tdi.getId(), quantity))
			.orElseGet(() -> itemStockRepository.decreaseStockIfEnough(itemId, quantity));
	}

	private boolean tryRestoreTimeDealStock(Long itemId, int quantity) {
		return timeDealItemRepository.findByItemId(itemId)
			.map(tdi -> itemStockRepository.restoreTimeDealStockIfPossible(tdi.getId(), quantity))
			.orElseGet(() -> itemStockRepository.restoreStockIfPossible(itemId, quantity));
	}

	private void registerLogAfterCommit(StockChangeEvent event) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				asyncStockHistoryUploader.upload(event);
			}
		});
	}
}