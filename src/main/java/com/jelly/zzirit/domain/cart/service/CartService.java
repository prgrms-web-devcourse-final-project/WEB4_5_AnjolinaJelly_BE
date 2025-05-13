package com.jelly.zzirit.domain.cart.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.cart.dto.response.CartItemFetchResponse;
import com.jelly.zzirit.domain.cart.dto.response.CartFetchResponse;
import com.jelly.zzirit.domain.cart.entity.Cart;
import com.jelly.zzirit.domain.cart.entity.CartItem;
import com.jelly.zzirit.domain.cart.mapper.CartItemMapper;
import com.jelly.zzirit.domain.cart.repository.CartItemRepository;
import com.jelly.zzirit.domain.cart.repository.CartRepository;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final TimeDealItemRepository timeDealItemRepository;
	private final ItemStockRepository itemStockRepository;

	public CartFetchResponse getMyCart(Long memberId) {

		// 사용자 장바구니 조회
		Cart cart = cartRepository.findByMemberId(memberId)
			.orElseGet(() -> {
				Member member = Member.builder().id(memberId).build();
				Cart newCart = Cart.builder().member(member).build();
				return cartRepository.save(newCart);
			});

		// 장바구니 항목 조회
		List<CartItem> cartItems = cartItemRepository.findAllWithItemByCartId(cart.getId());

		// itemId 목록 추출
		List<Long> itemIds = cartItems.stream()
			.map(cartItem -> cartItem.getItem().getId())
			.distinct()
			.toList();

		// ItemStock 일괄 조회 후 Map으로 캐싱
		Map<Long, ItemStock> itemStockMap = itemStockRepository.findAllByItemIdIn(itemIds).stream()
			.collect(Collectors.toMap(stock -> stock.getItem().getId(), stock -> stock));

		// DTO 변환
		List<CartItemFetchResponse> itemResponses = cartItems.stream()
			.map(cartItem -> {
				Item item = cartItem.getItem();

				ItemStock itemStock = itemStockMap.get(item.getId());
				if (itemStock == null) {
					throw new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND);
				}

				TimeDealItem timeDealItem = item.getItemStatus() == ItemStatus.TIME_DEAL
					? timeDealItemRepository.findActiveTimeDealItemByItemId(item.getId()).orElse(null)
					: null;

				return CartItemMapper.mapToCartItem(cartItem, itemStock, timeDealItem);
			}).toList();

		// 전체 수량 및 금액 집계 (품절 상품 제외)
		int cartTotalQuantity = itemResponses.stream()
			.filter(res -> !res.isSoldOut())
			.mapToInt(CartItemFetchResponse::quantity)
			.sum();

		int cartTotalPrice = itemResponses.stream()
			.filter(res -> !res.isSoldOut())
			.mapToInt(CartItemFetchResponse::totalPrice)
			.sum();

		return new CartFetchResponse(cart.getId(), itemResponses, cartTotalQuantity, cartTotalPrice);
	}
}