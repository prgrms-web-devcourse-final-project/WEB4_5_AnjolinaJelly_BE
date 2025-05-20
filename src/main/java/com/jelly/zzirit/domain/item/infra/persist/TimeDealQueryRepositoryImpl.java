package com.jelly.zzirit.domain.item.infra.persist;

import static com.jelly.zzirit.domain.item.entity.QItem.*;
import static com.jelly.zzirit.domain.item.entity.stock.QItemStock.*;
import static com.jelly.zzirit.domain.item.entity.timedeal.QTimeDeal.*;
import static com.jelly.zzirit.domain.item.entity.timedeal.QTimeDealItem.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.dto.request.TimeDealSearchCondition;
import com.jelly.zzirit.domain.item.dto.response.TimeDealFetchResponse;
import com.jelly.zzirit.domain.item.entity.QItem;
import com.jelly.zzirit.domain.item.entity.stock.QItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.QTimeDeal;
import com.jelly.zzirit.domain.item.entity.timedeal.QTimeDealItem;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDeal;
import com.jelly.zzirit.domain.item.repository.TimeDealQueryRepository;
import com.jelly.zzirit.global.dto.PageResponse;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeDealQueryRepositoryImpl implements TimeDealQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<TimeDealFetchResponse> search(TimeDealSearchCondition condition) {
		List<Tuple> tuples = queryFactory
			.select(timeDeal, timeDealItem, item, itemStock)
			.from(timeDealItem)
			.join(timeDealItem.timeDeal, timeDeal)
			.join(timeDealItem.item, item)
			.join(itemStock).on(itemStock.timeDealItem.id.eq(timeDealItem.id))
			.where(
				timeDealNameContains(condition.timeDealName()),
				timeDealIdEq(condition.timeDealId()),
				itemNameContains(condition.timeDealItemName()),
				timeDealItemIdEq(condition.timeDealItemId()),
				statusEq(condition.status())
			)
			.fetch();

		return tuples.stream()
			.collect(Collectors.groupingBy(
				tuple -> tuple.get(timeDeal),
				Collectors.mapping(tuple -> TimeDealFetchResponse.TimeDealFetchItem.from(
					tuple.get(timeDealItem).getId(),
					tuple.get(item).getName(),
					tuple.get(itemStock) != null ? tuple.get(itemStock).getQuantity() : 0,
					tuple.get(item).getPrice(),
					tuple.get(timeDealItem).getPrice()
				), Collectors.toList())
			))
			.entrySet().stream()
			.map(e -> TimeDealFetchResponse.from(e.getKey(), e.getValue()))
			.toList();
	}

	private BooleanExpression timeDealNameContains(String name) {
		return name != null ? timeDeal.name.contains(name) : null;
	}

	private BooleanExpression timeDealIdEq(Long id) {
		return id != null ? timeDeal.id.eq(id) : null;
	}

	private BooleanExpression itemNameContains(String name) {
		return name != null ? item.name.contains(name) : null;
	}

	private BooleanExpression timeDealItemIdEq(Long id) {
		return id != null ? timeDealItem.id.eq(id) : null;
	}

	private BooleanExpression statusEq(TimeDeal.TimeDealStatus status) {
		return status != null ? timeDeal.status.eq(status) : null;
	}

	@Override
	public PageResponse<TimeDealFetchResponse> searchWithPaging(TimeDealSearchCondition condition, int page, int size) {
		QTimeDeal timeDeal = QTimeDeal.timeDeal;
		QTimeDealItem timeDealItem = QTimeDealItem.timeDealItem;
		QItem item = QItem.item;
		QItemStock itemStock = QItemStock.itemStock;

		List<Tuple> tuples = queryFactory
			.select(timeDeal, timeDealItem, item, itemStock)
			.from(timeDealItem)
			.join(timeDealItem.timeDeal, timeDeal)
			.join(timeDealItem.item, item)
			.leftJoin(itemStock).on(itemStock.item.id.eq(item.id))
			.where(
				timeDealNameContains(condition.timeDealName()),
				timeDealIdEq(condition.timeDealId()),
				itemNameContains(condition.timeDealItemName()),
				timeDealItemIdEq(condition.timeDealItemId()),
				statusEq(condition.status())
			)
			.orderBy(timeDeal.createdAt.desc())
			.offset(page * size)
			.limit(size)
			.fetch();

		List<TimeDealFetchResponse> content = tuples.stream()
			.collect(Collectors.groupingBy(
				tuple -> tuple.get(timeDeal),
				Collectors.mapping(tuple -> TimeDealFetchResponse.TimeDealFetchItem.from(
					tuple.get(timeDealItem).getId(),
					tuple.get(item).getName(),
					tuple.get(itemStock) != null ? tuple.get(itemStock).getQuantity() : 0,
					tuple.get(item).getPrice(),
					tuple.get(timeDealItem).getPrice()
				), Collectors.toList())
			))
			.entrySet().stream()
			.map(e -> TimeDealFetchResponse.from(e.getKey(), e.getValue()))
			.toList();

		Long total = queryFactory
			.select(timeDeal.countDistinct())
			.from(timeDealItem)
			.join(timeDealItem.timeDeal, timeDeal)
			.join(timeDealItem.item, item)
			.where(
				timeDealNameContains(condition.timeDealName()),
				timeDealIdEq(condition.timeDealId()),
				itemNameContains(condition.timeDealItemName()),
				timeDealItemIdEq(condition.timeDealItemId()),
				statusEq(condition.status())
			)
			.fetchOne();

		return new PageResponse<>(
			content,
			page,
			size,
			total != null ? total : 0,
			(int)Math.ceil((double)(total != null ? total : 0) / size),
			content.size() < size
		);
	}
}