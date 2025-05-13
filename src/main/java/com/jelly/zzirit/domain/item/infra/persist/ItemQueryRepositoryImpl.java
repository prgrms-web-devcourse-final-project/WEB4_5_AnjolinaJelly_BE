package com.jelly.zzirit.domain.item.infra.persist;

import static com.jelly.zzirit.domain.item.entity.QBrand.*;
import static com.jelly.zzirit.domain.item.entity.QItem.*;
import static com.jelly.zzirit.domain.item.entity.QType.*;
import static com.jelly.zzirit.domain.item.entity.QTypeBrand.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
}
