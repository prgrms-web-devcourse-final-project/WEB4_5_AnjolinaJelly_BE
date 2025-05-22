package com.jelly.zzirit.domain.item.repository.stock;

public interface ItemStockRepositoryCustom {

	boolean decreaseStockIfEnough(Long itemId, int quantity);

	boolean restoreStockIfPossible(Long itemId, int quantity);

	boolean decreaseTimeDealStockIfEnough(Long timeDealItemId, int quantity);

	boolean restoreTimeDealStockIfPossible(Long timeDealItemId, int quantity);
}