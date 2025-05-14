package com.jelly.zzirit.domain.item.repository.stock;

public interface ItemStockRepositoryCustom {
	boolean decreaseStockIfEnough(Long itemId, int quantity);
}