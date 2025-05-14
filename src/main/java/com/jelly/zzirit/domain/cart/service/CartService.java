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
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final MemberRepository memberRepository;
	private final TimeDealItemRepository timeDealItemRepository;
	private final ItemStockRepository itemStockRepository;

	public CartFetchResponse getMyCart(Long memberId) {
		Cart cart = getOrCreateCart(memberId);
		List<CartItem> cartItems = cartItemRepository.findAllWithItemByCartId(cart.getId());
		Map<Long, ItemStock> stockMap = loadItemStockMap(cartItems);
		List<CartItemFetchResponse> responses = convertToResponses(cartItems, stockMap);

		return new CartFetchResponse(
			cart.getId(),
			responses,
			calculateTotalQuantity(responses),
			calculateTotalPrice(responses)
		);
	}

	private Cart getOrCreateCart(Long memberId) {
		return cartRepository.findByMemberId(memberId)
			.orElseGet(() -> {
				Member member = memberRepository.findById(memberId)
					.orElseThrow(() -> new InvalidUserException(BaseResponseStatus.USER_NOT_FOUND));
				return cartRepository.save(Cart.builder().member(member).build());
			});
	}

	private Map<Long, ItemStock> loadItemStockMap(List<CartItem> cartItems) {
		List<Long> itemIds = cartItems.stream()
			.map(cartItem -> cartItem.getItem().getId())
			.distinct()
			.toList();

		return itemStockRepository.findAllByItemIdIn(itemIds).stream()
			.collect(Collectors.toMap(stock -> stock.getItem().getId(), stock -> stock));
	}

	private List<CartItemFetchResponse> convertToResponses(List<CartItem> cartItems, Map<Long, ItemStock> stockMap) {
		return cartItems.stream()
			.map(cartItem -> {
				Item item = cartItem.getItem();
				ItemStock itemStock = stockMap.get(item.getId());
				if (itemStock == null) {
					throw new InvalidItemException(BaseResponseStatus.ITEM_STOCK_NOT_FOUND);
				}

				TimeDealItem timeDealItem = item.getItemStatus() == ItemStatus.TIME_DEAL
					? timeDealItemRepository.findActiveTimeDealItemByItemId(item.getId()).orElse(null)
					: null;

				return CartItemMapper.mapToCartItem(cartItem, itemStock, timeDealItem);
			}).toList();
	}

	private int calculateTotalQuantity(List<CartItemFetchResponse> responses) {
		return responses.stream()
			.filter(res -> !res.isSoldOut())
			.mapToInt(CartItemFetchResponse::quantity)
			.sum();
	}

	private int calculateTotalPrice(List<CartItemFetchResponse> responses) {
		return responses.stream()
			.filter(res -> !res.isSoldOut())
			.mapToInt(CartItemFetchResponse::totalPrice)
			.sum();
	}
}