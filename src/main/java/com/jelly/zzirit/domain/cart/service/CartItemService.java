package com.jelly.zzirit.domain.cart.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.cart.dto.request.CartItemCreateRequest;
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
import com.jelly.zzirit.domain.item.repository.ItemQueryRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.item.repository.stock.ItemStockRepository;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartItemService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final MemberRepository memberRepository;
	private final ItemQueryRepository itemQueryRepository;
	private final ItemStockRepository itemStockRepository;
	private final TimeDealItemRepository timeDealItemRepository;

	@Transactional
	public CartItemFetchResponse addItemToCart(Long memberId, CartItemCreateRequest request) {
		Cart cart = getOrCreateCart(memberId);
		Item item = getItemWithTypeJoin(request.itemId());
		TimeDealItem timeDealItem = getTimeDealItemIfApplicable(item);

		Optional<CartItem> existing = cartItemRepository.findCartItemWithAllJoins(cart.getId(), item.getId());
		int existingQuantity = existing.map(CartItem::getQuantity).orElse(0);
		int totalQuantity = existingQuantity + request.quantity();

		ItemStock itemStock = resolveItemStockOrThrow(item, timeDealItem, totalQuantity);

		CartItem cartItem = existing.map(ci -> {
			ci.setQuantity(totalQuantity);
			return ci;
		}).orElseGet(() -> {
			CartItem created = CartItem.of(cart, item, request.quantity());
			return cartItemRepository.save(created);
		});

		return CartItemMapper.mapToCartItem(cartItem, itemStock, timeDealItem);
	}

	@Transactional
	public void removeItemToCart(Long memberId, Long itemId) {
		Cart cart = getCart(memberId);
		CartItem cartItem = getCartItem(cart.getId(), itemId);
		cartItemRepository.delete(cartItem);
	}

	@Transactional
	public void removeItemsFromCart(Long memberId, List<Long> itemIds) {
		if (itemIds == null || itemIds.isEmpty())
			return;

		Long cartId = getCart(memberId).getId();
		List<Long> existingItemIds = cartItemRepository.findExistingItemIdsInCart(cartId, itemIds);
		if (existingItemIds.isEmpty()) {
			throw new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND_IN_CART);
		}

		cartItemRepository.deleteAllByCartIdAndItemIdIn(cartId, existingItemIds);
	}

	@Transactional
	public void removeAllItemsFromCart(Long memberId) {
		Long cartId = getCart(memberId).getId();
		cartItemRepository.deleteByCartId(cartId);
	}

	@Transactional
	public CartItemFetchResponse modifyQuantity(Long memberId, Long itemId, int delta) {
		Cart cart = getCart(memberId);
		CartItem cartItem = getCartItem(cart.getId(), itemId);
		Item item = cartItem.getItem();

		int newQuantity = cartItem.getQuantity() + delta;
		if (newQuantity < 1) {
			throw new InvalidItemException(BaseResponseStatus.INVALID_CART_QUANTITY);
		}

		TimeDealItem timeDealItem = getTimeDealItemIfApplicable(item);
		ItemStock itemStock = resolveItemStockOrThrow(item, timeDealItem, newQuantity);
		cartItem.changeQuantity(newQuantity);

		return CartItemMapper.mapToCartItem(cartItem, itemStock, timeDealItem, newQuantity);
	}


	private Cart getOrCreateCart(Long memberId) {
		return cartRepository.findByMemberId(memberId)
			.orElseGet(() -> {
				Member member = memberRepository.findById(memberId)
					.orElseThrow(() -> new InvalidUserException(BaseResponseStatus.USER_NOT_FOUND));
				return cartRepository.save(Cart.builder().member(member).build());
			});
	}

	private Cart getCart(Long memberId) {
		return cartRepository.findByMemberId(memberId)
			.orElseThrow(() -> new InvalidUserException(BaseResponseStatus.USER_NOT_FOUND));
	}

	private CartItem getCartItem(Long cartId, Long itemId) {
		return cartItemRepository.findByCartIdAndItemId(cartId, itemId)
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND_IN_CART));
	}

	private Item getItemWithTypeJoin(Long itemId) {
		return itemQueryRepository.findItemWithTypeJoin(itemId)
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));
	}

	private ItemStock getItemStock(Long itemId) {
		return itemStockRepository.findByItemId(itemId)
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_STOCK_NOT_FOUND));
	}

	private TimeDealItem getTimeDealItemIfApplicable(Item item) {
		if (item.getItemStatus() != ItemStatus.TIME_DEAL)
			return null;
		return timeDealItemRepository.findActiveTimeDealItemByItemId(item.getId()).orElse(null);
	}

	private ItemStock resolveItemStockOrThrow(Item item, TimeDealItem timeDealItem, int totalQuantity) {
		// 해당 itemId로 등록된 모든 재고 가져오기
		List<ItemStock> stocks = itemStockRepository.findAllByItemId(item.getId());

		// 타임딜 상품인 경우 → 타임딜 ID가 일치하는 재고 선택
		// 일반 상품인 경우 → timeDealItem이 null이고 itemId가 일치하는 재고 선택
		ItemStock target = (timeDealItem != null)
			? stocks.stream()
			.filter(s -> s.getTimeDealItem() != null && s.getTimeDealItem().getId().equals(timeDealItem.getId()))
			.findFirst()
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_STOCK_NOT_FOUND))
			: stocks.stream()
			.filter(s -> s.getTimeDealItem() == null && s.getItem().getId().equals(item.getId()))
			.findFirst()
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_STOCK_NOT_FOUND));

		// 장바구니 수량이 재고 수량보다 많으면 예외 발생
		if (totalQuantity > target.getQuantity()) {
			throw new InvalidItemException(BaseResponseStatus.CART_QUANTITY_EXCEEDS_STOCK);
		}

		return target;
	}
}