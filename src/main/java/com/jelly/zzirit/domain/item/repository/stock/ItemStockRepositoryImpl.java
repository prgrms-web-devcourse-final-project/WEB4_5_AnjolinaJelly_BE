package com.jelly.zzirit.domain.item.repository.stock;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.stock.QItemStock;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
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

	@Override
	public boolean decreaseTimeDealStockIfEnough(Long timeDealItemId, int quantity) {
		QItemStock stock = QItemStock.itemStock;

		long affectedRows = queryFactory.update(stock)
			.set(stock.quantity, stock.quantity.subtract(quantity))
			.set(stock.soldQuantity, stock.soldQuantity.add(quantity))
			.where(
				stock.timeDealItem.id.eq(timeDealItemId),
				stock.quantity.goe(quantity)
			)
			.execute();

		return affectedRows > 0;
	}

	@Override
	public boolean restoreTimeDealStockIfPossible(Long timeDealItemId, int quantity) {
		QItemStock stock = QItemStock.itemStock;

		long affectedRows = queryFactory.update(stock)
			.set(stock.quantity, stock.quantity.add(quantity))
			.set(stock.soldQuantity, stock.soldQuantity.subtract(quantity))
			.where(stock.timeDealItem.id.eq(timeDealItemId))
			.execute();

		return affectedRows > 0;

  @Override
	public List<ItemStock> findAllByItemId(List<Long> itemIds) {
		return queryFactory
			.selectFrom(QItemStock.itemStock)
			.where(QItemStock.itemStock.item.id.in(itemIds))
			.fetch();
	}
}