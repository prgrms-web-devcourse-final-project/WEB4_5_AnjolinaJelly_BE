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
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
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

	@Transactional
	public void removeOrderedItemsFromCart(Member member, List<Item> orderedItems) {
		cartItemRepository.deleteByCartMemberAndItemIn(member, orderedItems);
	}

	@Transactional
	public CartFetchResponse getMyCart(Long memberId) {
		Cart cart = getOrCreateCart(memberId);

		// 장바구니 항목 조회
		List<CartItem> cartItems = cartItemRepository.findAllWithItemByCartId(cart.getId());

		// itemId 목록 추출
		List<Long> itemIds = itemIds(cartItems);

		// 재고 조회 후 복합 키 기반 Map 구성
		List<ItemStock> stocks = itemStockRepository.findAllByItemIdIn(itemIds);
		Map<String, ItemStock> stockMap = loadItemStockMap(stocks);

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

	private List<Long> itemIds(List<CartItem> cartItems) {
		return cartItems.stream()
			.map(cartItem -> cartItem.getItem().getId())
			.distinct()
			.toList();
	}

	private String generateStockKey(Long itemId, Long timeDealItemId) {
		return itemId + ":" + (timeDealItemId != null ? timeDealItemId : "null");
	}

	private Map<String, ItemStock> loadItemStockMap(List<ItemStock> stocks) {
		return stocks.stream()
			.collect(Collectors.toMap(
				stock -> generateStockKey(
					stock.getItem().getId(),
					stock.getTimeDealItem() != null ? stock.getTimeDealItem().getId() : null
				),
				Function.identity()
			));
	}

	private List<CartItemFetchResponse> convertToResponses(List<CartItem> cartItems,
		Map<String, ItemStock> stockMap) {

		return cartItems.stream()
			.map(cartItem -> {
				Item item = cartItem.getItem();
				int quantity = cartItem.getQuantity();

				TimeDealItem timeDealItem = getTimeDealItemIfApplicable(item);

				// 복합 키 조회
				String key = generateStockKey(item.getId(), timeDealItem != null ? timeDealItem.getId() : null);
				ItemStock itemStock = stockMap.get(key);

				// 재고 없거나 0이면 품절 처리
				if (itemStock == null || itemStock.getQuantity() == 0) {
					ItemStock soldOutStock = markAsSoldOut(item);
					return CartItemMapper.mapToCartItem(cartItem, soldOutStock, timeDealItem, quantity);
				}

				// 수량 초과 시 1로 보정
				int finalQuantity = quantity > itemStock.getQuantity() ? 1 : quantity;

				return CartItemMapper.mapToCartItem(cartItem, itemStock, timeDealItem, finalQuantity);
			})
			.toList();
	}

	private TimeDealItem getTimeDealItemIfApplicable(Item item) {
		if (item.getItemStatus() != ItemStatus.TIME_DEAL)
			return null;
		return timeDealItemRepository.findActiveTimeDealItemByItemId(item.getId()).orElse(null);
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