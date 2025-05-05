package com.jelly.zzirit.domain.cart.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jelly.zzirit.domain.cart.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

	Optional<Cart> findByMemberId(Long memberId);

}
