package com.jelly.zzirit.domain.cart.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.cart.dto.response.CartItemResponse;
import com.jelly.zzirit.domain.cart.dto.response.CartResponse;
import com.jelly.zzirit.domain.cart.entity.Cart;
import com.jelly.zzirit.domain.cart.entity.CartItem;
import com.jelly.zzirit.domain.cart.repository.CartItemRepository;
import com.jelly.zzirit.domain.cart.repository.CartRepository;
import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.entity.ItemStatus;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final TimeDealItemRepository timeDealItemRepository;

	public CartResponse getMyCart(Long memberId) {

		// 1. 사용자 장바구니 조회 (없는 경우 예외 발생)
		Cart cart = cartRepository.findByMemberId(memberId)
			.orElseThrow(() -> new InvalidUserException(BaseResponseStatus.USER_NOT_FOUND));

		// 2. 장바구니 항목 조회
		List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.getId());

		// 3. 장바구니 항목 DTO로 변환
		List<CartItemResponse> itemResponses = cartItems.stream()
			.map(cartItem -> {
				Item item = cartItem.getItem();
				int quantity = cartItem.getQuantity();

				boolean isTimeDeal = item.getItemStatus() == ItemStatus.TIME_DEAL;
				Integer discountRatio = null;

				TimeDealItem timeDealItem = isTimeDeal
					? timeDealItemRepository.findActiveTimeDealItemByItemId(item.getId()).orElse(null)
					: null;

				int unitPrice = (timeDealItem != null)
					? timeDealItem.getPrice().intValue()
					: item.getPrice().intValue();

				discountRatio = (timeDealItem != null)
					? timeDealItem.getTimeDeal().getDiscountRatio()
					: null;

				int totalPrice = unitPrice * quantity;

				return new CartItemResponse(
					cartItem.getId(),
					item.getId(),
					item.getName(),
					item.getImageUrl(),
					quantity,
					unitPrice,
					totalPrice,
					isTimeDeal,
					discountRatio
				);
			})
			.toList();

		int totalQuantity = itemResponses.stream()
			.mapToInt(CartItemResponse::getQuantity)
			.sum();

		int totalAmount = itemResponses.stream()
			.mapToInt(CartItemResponse::getTotalPrice)
			.sum();

		return new CartResponse(cart.getId(), itemResponses, totalQuantity, totalAmount);
	}
}
