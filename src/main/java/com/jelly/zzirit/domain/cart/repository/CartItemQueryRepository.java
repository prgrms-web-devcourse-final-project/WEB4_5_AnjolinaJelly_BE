package com.jelly.zzirit.domain.cart.repository;

import java.util.List;
import java.util.Optional;

import com.jelly.zzirit.domain.cart.entity.CartItem;

public interface CartItemQueryRepository {

	List<CartItem> findAllWithItemByCartId(Long cartId);

	Optional<CartItem> findWithItemJoinByCartIdAndItemId(Long cartId, Long itemId);

	List<Long> findExistingItemIdsInCart(Long cartId, List<Long> itemIds);

	Optional<CartItem> findCartItemWithAllJoins(Long cartId, Long itemId);
}