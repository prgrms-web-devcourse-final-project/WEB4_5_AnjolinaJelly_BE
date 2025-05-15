package com.jelly.zzirit.domain.cart.service;

import java.util.List;

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
		ItemStock itemStock = getItemStock(item.getId());

		CartItem cartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())
			.map(existing -> {
				int newQuantity = existing.getQuantity() + request.quantity();
				if (newQuantity > itemStock.getQuantity()) {
					throw new InvalidItemException(BaseResponseStatus.CART_QUANTITY_EXCEEDS_STOCK);
				}
				existing.setQuantity(newQuantity);
				return existing;
			})
			.orElseGet(() -> {
				CartItem created = CartItem.of(cart, item, request.quantity());
				cartItemRepository.save(created);
				return created;
			});

		TimeDealItem timeDealItem = getTimeDealItemIfApplicable(item);

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
		if (itemIds == null || itemIds.isEmpty()) return;

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
		CartItem cartItem = cartItemRepository.findWithItemJoinByCartIdAndItemId(cart.getId(), itemId)
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND_IN_CART));

		int newQuantity = cartItem.getQuantity() + delta;
		if (newQuantity <= 0) {
			throw new InvalidItemException(BaseResponseStatus.INVALID_CART_QUANTITY);
		}

		Item item = cartItem.getItem();
		ItemStock itemStock = getItemStock(item.getId());
		if (newQuantity > itemStock.getQuantity()) {
			throw new InvalidItemException(BaseResponseStatus.CART_QUANTITY_EXCEEDS_STOCK);
		}

		cartItem.setQuantity(newQuantity);
		TimeDealItem timeDealItem = getTimeDealItemIfApplicable(item);
		return CartItemMapper.mapToCartItem(cartItem, itemStock, timeDealItem);
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
}