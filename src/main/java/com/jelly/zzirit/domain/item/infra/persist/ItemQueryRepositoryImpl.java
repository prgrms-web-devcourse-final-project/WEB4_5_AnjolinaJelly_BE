package com.jelly.zzirit.domain.item.infra.persist;

import static com.jelly.zzirit.domain.item.entity.QBrand.*;
import static com.jelly.zzirit.domain.item.entity.QItem.*;
import static com.jelly.zzirit.domain.item.entity.QType.*;
import static com.jelly.zzirit.domain.item.entity.QTypeBrand.*;

import java.util.List;

import com.jelly.zzirit.domain.admin.dto.response.AdminItemFetchResponse;
import com.jelly.zzirit.domain.item.entity.*;
import com.jelly.zzirit.domain.item.entity.stock.QItemStock;
import com.querydsl.core.types.Projections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
	public Page<Item> findItems(List<String> types, List<String> brands, String keyword, String sort, Pageable pageable) {
		List<Item> pagingItems = queryFactory.selectFrom(item)
			.join(item.typeBrand, typeBrand).fetchJoin()
			.join(typeBrand.type, type).fetchJoin()
			.join(typeBrand.brand, brand).fetchJoin()
			.where(
				isContainKeyword(keyword),
				isTypeContain(types),
				isBrandContain(brands)
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(sortByPrice(sort))
			.fetch();

		JPAQuery<Long> total = queryFactory.select(item.count())
			.from(item)
			.join(item.typeBrand, typeBrand)
			.join(typeBrand.type, type)
			.join(typeBrand.brand, brand)
			.where(
				isContainKeyword(keyword),
				isTypeContain(types),
				isBrandContain(brands)
			);

		return PageableExecutionUtils
			.getPage(
				pagingItems,
				pageable,
				total::fetchOne
			);
	}

	private OrderSpecifier<?> sortByPrice(String sort) {
		if (sort.equals("priceDesc")) {
			return item.price.desc();
		}
		return item.price.asc();
	}

	private BooleanExpression isContainKeyword(String keyword) {
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
	public Page<AdminItemFetchResponse> findAdminItems(String name, Long itemId, Pageable pageable) {
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
				.where(
						containsName(name),
						matchesItemId(itemId)
				)
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
				.where(
						containsName(name),
						matchesItemId(itemId)
				)
				.fetchOne();

		return PageableExecutionUtils.getPage(content, pageable, () -> count == null ? 0 : count);
	}

	private BooleanExpression containsName(String name) {
		return (name == null || name.isBlank()) ? null : QItem.item.name.lower().contains(name.toLowerCase());
	}

	private BooleanExpression matchesItemId(Long id) {
		return id == null ? null : QItem.item.id.eq(id);
	}
}
