package com.jelly.zzirit.domain.cart.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.cart.dto.response.CartFetchResponse;
import com.jelly.zzirit.domain.cart.dto.response.CartItemFetchResponse;
import com.jelly.zzirit.domain.cart.entity.Cart;
import com.jelly.zzirit.domain.cart.entity.CartItem;
import com.jelly.zzirit.domain.cart.mapper.CartItemMapper;
import com.jelly.zzirit.domain.cart.repository.CartItemRepository;
import com.jelly.zzirit.domain.cart.repository.CartRepository;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final MemberRepository memberRepository;
	private final TimeDealItemRepository timeDealItemRepository;
	private final ItemStockRepository itemStockRepository;

	@Transactional
	public void removeOrderedItemsFromCart(Member member, List<Item> orderedItems) {
		cartItemRepository.deleteByCartMemberAndItemIn(member, orderedItems);
	}

	@Transactional
	public CartFetchResponse getMyCart(Long memberId) {
		Cart cart = getOrCreateCart(memberId);
		List<CartItem> cartItems = cartItemRepository.findAllWithItemByCartId(cart.getId());
		List<Long> itemIds = itemIds(cartItems);

		Map<Long, TimeDealItem> timeDealItemMap = loadActiveTimeDealItemMap(itemIds);
		Map<Long, ItemStock> normalStockMap = loadNormalItemStockMap(itemIds);

		List<CartItemFetchResponse> responses = convertToCartResponses(cartItems, timeDealItemMap, normalStockMap);

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

	private List<Long> itemIds(List<CartItem> cartItems) {
		return cartItems.stream()
			.map(cartItem -> cartItem.getItem().getId())
			.distinct()
			.toList();
	}

	private List<CartItemFetchResponse> convertToCartResponses(List<CartItem> cartItems,
		Map<Long, TimeDealItem> timeDealItemMap,
		Map<Long, ItemStock> normalStockMap) {
		return cartItems.stream()
			.map(cartItem -> {
				Item item = cartItem.getItem();

				TimeDealItem timeDealItem = timeDealItemMap.get(item.getId());

				ItemStock itemStock = (timeDealItem != null)
					? itemStockRepository.findByTimeDealItem(timeDealItem).orElse(null)
					: normalStockMap.get(item.getId());

				int stockQuantity = (itemStock != null) ? itemStock.getQuantity() : 0;

				if (stockQuantity == 0) {
					return CartItemMapper.mapToCartItem(cartItem, markAsSoldOut(item), timeDealItem, 0);
				}

				int currentQuantity = cartItem.getQuantity();
				int finalQuantity = Math.min(currentQuantity, stockQuantity);

				if (finalQuantity != currentQuantity) {
					cartItem.changeQuantity(finalQuantity);
					cartItemRepository.save(cartItem);
				}
				return CartItemMapper.mapToCartItem(cartItem, itemStock, timeDealItem, finalQuantity);
			})
			.toList();
	}

	private Map<Long, TimeDealItem> loadActiveTimeDealItemMap(List<Long> itemIds) {
		return timeDealItemRepository.findActiveByItemIds(itemIds).stream()
			.collect(Collectors.toMap(t -> t.getItem().getId(), Function.identity()));
	}

	private Map<Long, ItemStock> loadNormalItemStockMap(List<Long> itemIds) {
		return itemStockRepository.findAllByItemIdIn(itemIds).stream()
			.collect(Collectors.toMap(s -> s.getItem().getId(), Function.identity()));
	}

	private ItemStock markAsSoldOut(Item item) {
		return ItemStock.builder()
			.item(item)
			.quantity(0)
			.soldQuantity(0)
			.timeDealItem(null)
			.build();
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