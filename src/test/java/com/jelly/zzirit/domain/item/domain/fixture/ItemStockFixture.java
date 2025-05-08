package com.jelly.zzirit.domain.item.domain.fixture;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;

public class ItemStockFixture {

	public static ItemStock 풀재고_상품(Item item) {
		return ItemStock.builder()
			.item(item)
			.quantity(20)
			.soldQuantity(0)
			.build();
	}
}
