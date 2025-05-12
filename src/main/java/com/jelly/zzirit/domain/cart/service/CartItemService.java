package com.jelly.zzirit.domain.cart.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.cart.dto.request.CartItemCreateRequest;
import com.jelly.zzirit.domain.cart.dto.response.CartItemFetchResponse;
import com.jelly.zzirit.domain.cart.entity.Cart;
import com.jelly.zzirit.domain.cart.entity.CartItem;
import com.jelly.zzirit.domain.cart.repository.CartItemRepository;
import com.jelly.zzirit.domain.cart.repository.CartRepository;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartItemService {

	private final CartItemRepository cartItemRepository;
	private final CartRepository cartRepository;
	private final TimeDealItemRepository timeDealItemRepository;
	private final ItemRepository itemRepository;
	private final ItemStockRepository itemStockRepository;

	private final CartService cartService;

	@Transactional
	public CartItemFetchResponse addItemToCart(Long memberId, CartItemCreateRequest request) {

		Cart cart = cartRepository.findByMemberId(memberId)
			.orElseGet(() -> {
				Member member = Member.builder().id(memberId).build();
				Cart newCart = Cart.builder().member(member).build();
				return cartRepository.save(newCart);
			});

		Item item = itemRepository.findById(request.itemId())
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));

		// 장바구니에 이미 존재하는 상품인지 확인
		Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());
		CartItem cartItem;
		if (existingCartItem.isPresent()) {
			cartItem = existingCartItem.get();
			cartItem.increaseQuantity(request.quantity()); // 있으면 수량 증가
		} else {
			cartItem = CartItem.of(cart, item, request.quantity());
			cartItemRepository.save(cartItem);
		}

		int originalPrice = item.getPrice().intValue();
		int discountedPrice = originalPrice;
		Integer discountRatio = null;

		boolean isTimeDeal = item.getItemStatus() == ItemStatus.TIME_DEAL;

		if (isTimeDeal) {
			TimeDealItem timeDealItem = timeDealItemRepository.findActiveTimeDealItemByItemId(item.getId())
				.orElse(null);
			if (timeDealItem != null) {
				discountedPrice = timeDealItem.getPrice().intValue();
				discountRatio = timeDealItem.getTimeDeal().getDiscountRatio();
			}
		}

		int totalPrice = discountedPrice * cartItem.getQuantity();

		// 재고 확인
		ItemStock itemStock = itemStockRepository.findByItemId(item.getId())
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));
		boolean isSoldOut = itemStock.getQuantity() == 0;

		return new CartItemFetchResponse(
			cartItem.getId(),
			item.getId(),
			item.getName(),
			item.getTypeBrand().getType().getName(),
			item.getTypeBrand().getBrand().getName(),
			cartItem.getQuantity(),
			item.getImageUrl(),
			originalPrice,
			discountedPrice,
			totalPrice,
			isTimeDeal,
			discountRatio,
			isSoldOut
		);
	}

	@Transactional
	public void removeItemToCart(Long memberId, Long itemId) {

		Cart cart = cartRepository.findByMemberId(memberId)
			.orElseThrow(() -> new InvalidUserException(BaseResponseStatus.USER_NOT_FOUND));

		CartItem cartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), itemId)
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND_IN_CART));

		cartItemRepository.delete(cartItem);
	}

	@Transactional
	public CartItemFetchResponse modifyQuantity(Long memberId, Long itemId, int delta) {

		Cart cart = cartRepository.findByMemberId(memberId)
			.orElseThrow(() -> new InvalidUserException(BaseResponseStatus.USER_NOT_FOUND));

		CartItem cartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), itemId)
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND_IN_CART));

		int newQuantity = cartItem.getQuantity() + delta;
		if (newQuantity <= 0) {
			throw new InvalidItemException(BaseResponseStatus.INVALID_CART_QUANTITY);
		}

		Item item = cartItem.getItem();

		ItemStock itemStock = itemStockRepository.findByItemId(itemId)
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_STOCK_NOT_FOUND));
		if (newQuantity > itemStock.getQuantity()) {
			throw new InvalidItemException(BaseResponseStatus.CART_QUANTITY_EXCEEDS_STOCK);
		}

		cartItem.setQuantity(newQuantity);

		int originalPrice = item.getPrice().intValue();
		int discountedPrice = originalPrice;
		Integer discountRatio = null;
		boolean isTimeDeal = item.getItemStatus() == ItemStatus.TIME_DEAL;

		if (isTimeDeal) {
			TimeDealItem timeDealItem = timeDealItemRepository.findActiveTimeDealItemByItemId(item.getId())
				.orElse(null);
			if (timeDealItem != null) {
				discountedPrice = timeDealItem.getPrice().intValue();
				discountRatio = timeDealItem.getTimeDeal().getDiscountRatio();
			}
		}

		int totalPrice = discountedPrice * newQuantity;
		boolean isSoldOut = itemStock.getQuantity() == 0;

		return new CartItemFetchResponse(
			cartItem.getId(),
			item.getId(),
			item.getName(),
			item.getTypeBrand().getType().getName(),
			item.getTypeBrand().getBrand().getName(),
			newQuantity,
			item.getImageUrl(),
			originalPrice,
			discountedPrice,
			totalPrice,
			isTimeDeal,
			discountRatio,
			isSoldOut
		);
	}

}
