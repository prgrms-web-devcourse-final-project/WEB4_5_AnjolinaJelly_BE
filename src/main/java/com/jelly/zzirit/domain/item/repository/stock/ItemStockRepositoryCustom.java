package com.jelly.zzirit.domain.item.repository.stock;

import java.util.List;

import com.jelly.zzirit.domain.item.entity.stock.ItemStock;

public interface ItemStockRepositoryCustom {

	boolean decreaseStockIfEnough(Long itemId, int quantity);

	boolean restoreStockIfPossible(Long itemId, int quantity);

	boolean decreaseTimeDealStockIfEnough(Long timeDealItemId, int quantity);

	boolean restoreTimeDealStockIfPossible(Long timeDealItemId, int quantity);

	List<ItemStock> findAllByItemId(List<Long> itemIds);

}