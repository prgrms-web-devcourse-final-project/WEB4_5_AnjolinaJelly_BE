package com.jelly.zzirit.domain.cart.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
		List<CartItem> cartItems = cartItemRepository.findAllWithItemByCartId(cart.getId());
		Map<Long, List<ItemStock>> stockGroupMap = loadGroupedItemStocks(cartItems);
		List<CartItemFetchResponse> responses = convertToResponses(cartItems, stockGroupMap);

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

	private Map<Long, List<ItemStock>> loadGroupedItemStocks(List<CartItem> cartItems) {
		List<Long> itemIds = cartItems.stream()
			.map(cartItem -> cartItem.getItem().getId())
			.distinct()
			.toList();

		List<ItemStock> stocks = itemStockRepository.findAllByItemId(itemIds);

		return stocks.stream()
			.collect(Collectors.groupingBy(stock -> stock.getItem().getId()));
	}

	private List<CartItemFetchResponse> convertToResponses(List<CartItem> cartItems,
		Map<Long, List<ItemStock>> stockGroupMap) {
		return cartItems.stream()
			.map(cartItem -> {
				Item item = cartItem.getItem();
				int originalQuantity = cartItem.getQuantity();

				TimeDealItem timeDealItem = getTimeDealItemIfApplicable(item);

				Optional<ItemStock> stockOptional = selectStockForItem(item, timeDealItem,
					stockGroupMap.getOrDefault(item.getId(), List.of()));

				if (stockOptional.isEmpty() || stockOptional.get().getQuantity() == 0) {
					ItemStock soldOutStock = markAsSoldOut(item);
					return CartItemMapper.mapToCartItem(cartItem, soldOutStock, timeDealItem, 0);
				}

				ItemStock itemStock = stockOptional.get();

				// 재고보다 많은 수량일 경우 수량을 1로 조정하여 응답에 반영
				int finalQuantity = originalQuantity > itemStock.getQuantity() ? 1 : originalQuantity;

				return CartItemMapper.mapToCartItem(cartItem, itemStock, timeDealItem, finalQuantity);
			})
			.toList();
	}

	private Optional<ItemStock> selectStockForItem(Item item, TimeDealItem timeDealItem, List<ItemStock> stocks) {
		if (timeDealItem != null) {
			return stocks.stream()
				.filter(stock -> stock.getTimeDealItem() != null && stock.getTimeDealItem()
					.getId()
					.equals(timeDealItem.getId()))
				.findFirst();
		} else {
			return stocks.stream()
				.filter(stock -> stock.getTimeDealItem() == null && stock.getItem().getId().equals(item.getId()))
				.findFirst();
		}
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
			.filter(res -> res.quantity() > 0)
			.mapToInt(CartItemFetchResponse::quantity)
			.sum();
	}

	private int calculateTotalPrice(List<CartItemFetchResponse> responses) {
		return responses.stream()
			.filter(res -> res.quantity() > 0)
			.mapToInt(CartItemFetchResponse::totalPrice)
			.sum();
	}
}