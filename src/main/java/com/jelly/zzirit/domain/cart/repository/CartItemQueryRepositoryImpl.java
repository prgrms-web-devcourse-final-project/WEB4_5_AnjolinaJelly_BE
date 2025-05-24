package com.jelly.zzirit.domain.cart.repository;

import static com.jelly.zzirit.domain.cart.entity.QCartItem.*;
import static com.jelly.zzirit.domain.item.entity.QBrand.*;
import static com.jelly.zzirit.domain.item.entity.QItem.*;
import static com.jelly.zzirit.domain.item.entity.QType.*;
import static com.jelly.zzirit.domain.item.entity.QTypeBrand.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.cart.entity.CartItem;
import com.jelly.zzirit.domain.cart.entity.QCartItem;
import com.jelly.zzirit.domain.item.entity.QBrand;
import com.jelly.zzirit.domain.item.entity.QItem;
import com.jelly.zzirit.domain.item.entity.QType;
import com.jelly.zzirit.domain.item.entity.QTypeBrand;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartItemQueryRepositoryImpl implements CartItemQueryRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<CartItem> findAllWithItemByCartId(Long cartId) {
		return queryFactory
			.selectFrom(cartItem)
			.join(cartItem.item, item).fetchJoin()
			.join(item.typeBrand, typeBrand).fetchJoin()
			.join(typeBrand.type, type).fetchJoin()
			.join(typeBrand.brand, brand).fetchJoin()
			.where(cartItem.cart.id.eq(cartId))
			.fetch();
	}

	@Override
	public Optional<CartItem> findWithItemJoinByCartIdAndItemId(Long cartId, Long itemId) {
		return Optional.ofNullable(queryFactory
			.selectFrom(cartItem)
			.join(cartItem.item, item).fetchJoin()
			.join(item.typeBrand, typeBrand).fetchJoin()
			.join(typeBrand.type, type).fetchJoin()
			.join(typeBrand.brand, brand).fetchJoin()
			.where(cartItem.cart.id.eq(cartId), cartItem.item.id.eq(itemId))
			.fetchOne());
	}

	@Override
	public List<Long> findExistingItemIdsInCart(Long cartId, List<Long> itemIds) {
		if (itemIds == null || itemIds.isEmpty()) return List.of();

		return queryFactory
			.select(cartItem.item.id)
			.from(cartItem)
			.where(
				cartItem.cart.id.eq(cartId),
				cartItem.item.id.in(itemIds)
			)
			.fetch();
	}

	@Override
	public Optional<CartItem> findCartItemWithAllJoins(Long cartId, Long itemId) {
		QCartItem cartItem = QCartItem.cartItem;
		QItem item = QItem.item;
		QTypeBrand typeBrand = QTypeBrand.typeBrand;
		QType type = QType.type;
		QBrand brand = QBrand.brand;

		return Optional.ofNullable(
			queryFactory
				.selectFrom(cartItem)
				.join(cartItem.item, item).fetchJoin()
				.join(item.typeBrand, typeBrand).fetchJoin()
				.join(typeBrand.type, type).fetchJoin()
				.join(typeBrand.brand, brand).fetchJoin()
				.where(
					cartItem.cart.id.eq(cartId),
					cartItem.item.id.eq(itemId)
				)
				.fetchOne()
		);
	}

}