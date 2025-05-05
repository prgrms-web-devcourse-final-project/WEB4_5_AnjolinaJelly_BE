package com.jelly.zzirit.domain.cart.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.cart.dto.request.CartItemAddRequest;
import com.jelly.zzirit.domain.cart.dto.response.CartItemResponse;
import com.jelly.zzirit.domain.cart.entity.Cart;
import com.jelly.zzirit.domain.cart.entity.CartItem;
import com.jelly.zzirit.domain.cart.repository.CartItemRepository;
import com.jelly.zzirit.domain.cart.repository.CartRepository;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
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

	@Transactional
	public CartItemResponse addItemToCart(Long memberId, CartItemAddRequest request) {

		Cart cart = cartRepository.findByMemberId(memberId)
			.orElseGet(() -> {
				Member member = Member.builder().id(memberId).build();
				Cart newCart = Cart.builder().member(member).build();
				return cartRepository.save(newCart);
			});

		Item item = itemRepository.findById(request.getItemId())
			.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));

		// 장바구니에 이미 존재하는 상품인지 확인
		Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());
		CartItem cartItem;
		if (existingCartItem.isPresent()) {
			cartItem = existingCartItem.get();
			cartItem.increaseQuantity(request.getQuantity()); // 있으면 수량 증가
		} else {
			cartItem = CartItem.of(cart, item, request.getQuantity());
			cartItemRepository.save(cartItem);
		}

		int unitPrice = item.getPrice().intValue();
		int discountedPrice = unitPrice;
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

		return new CartItemResponse(
			cartItem.getId(),
			item.getId(),
			item.getName(),
			item.getImageUrl(),
			cartItem.getQuantity(),
			// unitPrice,       // 정가, 현재는 할인 적용 변수에 정가를 반영 중이라 반대로 주석처리
			discountedPrice,     // TODO: 할인 가격 (FE 협의 후 노출)
			totalPrice,
			isTimeDeal,
			discountRatio
		);
	}


}
