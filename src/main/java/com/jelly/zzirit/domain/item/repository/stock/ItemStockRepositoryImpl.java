package com.jelly.zzirit.domain.item.repository.stock;

import com.jelly.zzirit.domain.item.entity.stock.QItemStock;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ItemStockRepositoryImpl implements ItemStockRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public boolean decreaseStockIfEnough(Long itemId, int quantity) {
		QItemStock stock = QItemStock.itemStock;

		long affectedRows = queryFactory.update(stock)
			.set(stock.quantity, stock.quantity.subtract(quantity))
			.set(stock.soldQuantity, stock.soldQuantity.add(quantity))
			.where(
				stock.item.id.eq(itemId),
				stock.quantity.goe(quantity)
			)
			.execute();

		return affectedRows > 0;
	}

	@Override
	public boolean restoreStockIfPossible(Long itemId, int quantity) {
		QItemStock stock = QItemStock.itemStock;

		long affectedRows = queryFactory.update(stock)
			.set(stock.quantity, stock.quantity.add(quantity))
			.set(stock.soldQuantity, stock.soldQuantity.subtract(quantity))
			.where(stock.item.id.eq(itemId))
			.execute();

		return affectedRows > 0;
	}
}