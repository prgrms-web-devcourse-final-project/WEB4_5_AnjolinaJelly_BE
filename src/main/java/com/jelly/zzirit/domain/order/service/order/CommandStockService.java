package com.jelly.zzirit.domain.order.service.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.domain.order.dto.StockChangeEvent;
import com.jelly.zzirit.domain.order.util.AsyncStockHistoryUploader;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import com.jelly.zzirit.global.redis.lock.DistributedLock;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommandStockService {

	private final ItemStockRepository itemStockRepository;
	private final AsyncStockHistoryUploader asyncStockHistoryUploader;

	@DistributedLock(
		key = "#itemId",
		leaseTime = 12L
	)
	@Transactional
	public void decrease(String orderNumber, Long itemId, int quantity) {
		boolean success = itemStockRepository.decreaseStockIfEnough(itemId, quantity);

		if (!success) {
			throw new InvalidOrderException(BaseResponseStatus.STOCK_REDUCE_FAILED);
		}

		registerLogAfterCommit(StockChangeEvent.decrease(itemId, orderNumber, quantity));
	}

	@DistributedLock(
		key = "#itemId",
		leaseTime = 12L
	)
	@Transactional
	public void restore(String orderNumber, Long itemId, int quantity) {
		boolean success = itemStockRepository.restoreStockIfPossible(itemId, quantity);

		if (!success) {
			throw new InvalidOrderException(BaseResponseStatus.STOCK_RESTORE_FAILED);
		}

		registerLogAfterCommit(StockChangeEvent.restore(itemId, orderNumber, quantity));
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