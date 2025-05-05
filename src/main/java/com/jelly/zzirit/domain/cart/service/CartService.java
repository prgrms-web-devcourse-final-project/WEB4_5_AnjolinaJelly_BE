package com.jelly.zzirit.domain.cart.service;

import java.math.BigDecimal;
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
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final ItemRepository itemRepository;
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
				BigDecimal originPrice = item.getPrice();
				int unitPrice = originPrice.intValue();

				// 타임딜 적용 여부
				boolean isTimeDeal = item.getItemStatus() == ItemStatus.TIME_DEAL;
				Integer discountRatio = null;
				int discountedPrice = unitPrice;

				if (isTimeDeal) {
					// 타임딜 정보 조회
					TimeDealItem timeDealItem = timeDealItemRepository
						.findActiveTimeDealItemByItemId(item.getId())
						.orElse(null);

					if (timeDealItem != null) {
						BigDecimal dealPrice = timeDealItem.getPrice();
						discountedPrice = dealPrice.intValue();

						discountRatio = timeDealItem.getTimeDeal().getDiscountRatio();
					}
				}

				int totalPrice = discountedPrice * quantity;

				return new CartItemResponse(
					cartItem.getId(),
					item.getId(),
					item.getName(),
					item.getImageUrl(),
					quantity,
					unitPrice,
					// , discountedUnitPrice // 추후 FE 협의 후 포함 가능
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
