package com.jelly.zzirit.domain.cart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jelly.zzirit.domain.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long>, CartItemQueryRepository {

	List<CartItem> findAllByCartId(Long cartId);

	Optional<CartItem> findByCartIdAndItemId(Long cartId, Long itemId);

}