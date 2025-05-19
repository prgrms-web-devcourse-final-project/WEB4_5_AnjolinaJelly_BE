package com.jelly.zzirit.domain.item.infra.persist;

import static com.jelly.zzirit.domain.item.entity.QBrand.*;
import static com.jelly.zzirit.domain.item.entity.QItem.*;
import static com.jelly.zzirit.domain.item.entity.QType.*;
import static com.jelly.zzirit.domain.item.entity.QTypeBrand.*;
import static com.jelly.zzirit.domain.item.entity.timedeal.QTimeDealItem.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.jelly.zzirit.domain.admin.dto.response.AdminItemFetchResponse;
import com.jelly.zzirit.domain.item.entity.*;
import com.jelly.zzirit.domain.item.entity.stock.QItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.querydsl.core.types.Projections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.dto.request.ItemFilterRequest;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.repository.ItemQueryRepository;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemQueryRepositoryImpl implements ItemQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<TimeDealItem> findItems(ItemFilterRequest filter, String sort, Pageable pageable) {
		List<TimeDealItem> pagingItems = queryFactory.selectFrom(timeDealItem)
			.join(timeDealItem.item, item).fetchJoin()
			.join(item.typeBrand, typeBrand).fetchJoin()
			.join(typeBrand.type, type).fetchJoin()
			.join(typeBrand.brand, brand).fetchJoin()
			.where(
				timeDealItem.timeDeal.endTime.after(LocalDateTime.now()),
				isKeywordContain(filter.keyword()),
				isTypeContain(filter.types()),
				isBrandContain(filter.brands())
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(sortByPrice(sort))
			.fetch();

		JPAQuery<Long> total = queryFactory.select(timeDealItem.count())
			.from(timeDealItem)
			.join(timeDealItem.item, item).fetchJoin()
			.join(item.typeBrand, typeBrand).fetchJoin()
			.join(typeBrand.type, type).fetchJoin()
			.join(typeBrand.brand, brand).fetchJoin()
			.where(
				timeDealItem.timeDeal.endTime.after(LocalDateTime.now()),
				isKeywordContain(filter.keyword()),
				isTypeContain(filter.types()),
				isBrandContain(filter.brands())
			);

		return PageableExecutionUtils
			.getPage(
				pagingItems,
				pageable,
				total::fetchOne
			);
	}

	@Override
	public Optional<Item> findItemWithTypeJoin(Long itemId) {
		return Optional.ofNullable(
			queryFactory.selectFrom(item)
				.join(item.typeBrand, typeBrand).fetchJoin()
				.join(typeBrand.type, type).fetchJoin()
				.join(typeBrand.brand, brand).fetchJoin()
				.where(item.id.eq(itemId))
				.fetchOne()
		);
	}

	private OrderSpecifier<?> sortByPrice(String sort) {
		if (sort.equals("priceDesc")) {
			return item.price.desc();
		}
		return item.price.asc();
	}

	private BooleanExpression isKeywordContain(String keyword) {
		if (keyword == null) {
			return null;
		}
		return item.name.containsIgnoreCase(keyword)
			.or(brand.name.containsIgnoreCase(keyword))
			.or(type.name.containsIgnoreCase(keyword));
	}

	private BooleanExpression isTypeContain(List<String> types) {
		if (types == null || types.isEmpty()) {
			return null;
		}
		return type.name.in(types);
	}

	private BooleanExpression isBrandContain(List<String> brands) {
		if (brands == null || brands.isEmpty()) {
			return null;
		}
		return brand.name.in(brands);
	}

	@Override
	public Optional<AdminItemFetchResponse> findAdminItemById(Long itemId) {
		QItem i = QItem.item;
		QItemStock s = QItemStock.itemStock;
		QTypeBrand tb = QTypeBrand.typeBrand;
		QType t = QType.type;
		QBrand b = QBrand.brand;

		AdminItemFetchResponse dto = queryFactory
				.select(Projections.constructor(AdminItemFetchResponse.class,
						i.id,
						i.name,
						i.imageUrl,
						t.name,
						b.name,
						i.price,
						s.quantity
				))
				.from(i)
				.join(i.typeBrand, tb)
				.join(tb.type, t)
				.join(tb.brand, b)
				.join(s).on(s.item.eq(i))
				.where(i.id.eq(itemId))
				.fetchOne(); // 단건 조회

		return Optional.ofNullable(dto);
	}

	@Override
	public Page<AdminItemFetchResponse> findAdminItems(String name, Pageable pageable) {
		QItem i = QItem.item;
		QItemStock s = QItemStock.itemStock;
		QTypeBrand tb = QTypeBrand.typeBrand;
		QType t = QType.type;
		QBrand b = QBrand.brand;

		List<AdminItemFetchResponse> content = queryFactory
				.select(Projections.constructor(AdminItemFetchResponse.class,
						i.id,
						i.name,
						i.imageUrl,
						t.name,
						b.name,
						i.price,
						s.quantity
				))
				.from(i)
				.join(i.typeBrand, tb)
				.join(tb.type, t)
				.join(tb.brand, b)
				.join(s).on(s.item.eq(i))
				.where(containsName(name)) // ✅ 이름 조건만 남김
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();

		Long count = queryFactory
				.select(i.count())
				.from(i)
				.join(i.typeBrand, tb)
				.join(tb.type, t)
				.join(tb.brand, b)
				.join(s).on(s.item.eq(i))
				.where(containsName(name)) // ✅ 이름 조건만 남김
				.fetchOne();

		return PageableExecutionUtils.getPage(content, pageable, () -> count == null ? 0 : count);
	}

	private BooleanExpression containsName(String name) {
		return (name == null || name.isBlank()) ? null : QItem.item.name.lower().contains(name.toLowerCase());
	}
}
