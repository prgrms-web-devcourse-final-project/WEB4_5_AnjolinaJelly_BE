package com.jelly.zzirit.domain.order.service;

import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.order.repository.ItemStockRepository;
import com.jelly.zzirit.domain.order.repository.TimeDealStockRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockConfirmer {

	private final ItemStockRepository itemStockRepository;
	private final TimeDealStockRepository timeDealStockRepository;

	public int confirmItemStock(Long itemId, int quantity) {
		return itemStockRepository.confirmStock(itemId, quantity);
	}

	public int confirmTimeDealStock(Long timeDealItemId, int quantity) {
		return timeDealStockRepository.confirmStock(timeDealItemId, quantity);
	}
}