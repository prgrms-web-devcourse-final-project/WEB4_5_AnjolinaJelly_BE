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
import com.jelly.zzirit.domain.item.entity.stock.ItemStock;
import com.jelly.zzirit.domain.item.entity.timedeal.TimeDealItem;
import com.jelly.zzirit.domain.item.repository.ItemStockRepository;
import com.jelly.zzirit.domain.item.repository.TimeDealItemRepository;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidItemException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final TimeDealItemRepository timeDealItemRepository;
	private final ItemStockRepository itemStockRepository;

	public CartResponse getMyCart(Long memberId) {

		// 사용자 장바구니 조회
		Cart cart = cartRepository.findByMemberId(memberId)
			.orElseGet(() -> {
				Member member = Member.builder().id(memberId).build();
				Cart newCart = Cart.builder().member(member).build();
				return cartRepository.save(newCart);
			});

		// 장바구니 항목 조회
		List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.getId());

		List<CartItemResponse> itemResponses = cartItems.stream()
			.map(cartItem -> {
				Item item = cartItem.getItem();
				int quantity = cartItem.getQuantity();
				int unitPrice = item.getPrice().intValue();

				// 재고 확인
				ItemStock itemStock = itemStockRepository.findByItemId(item.getId())
					.orElseThrow(() -> new InvalidItemException(BaseResponseStatus.ITEM_NOT_FOUND));
				boolean isSoldOut = itemStock.getQuantity() == 0;

				// 타임딜 여부
				boolean isTimeDeal = item.getItemStatus() == ItemStatus.TIME_DEAL;
				Integer discountRatio = null;
				int discountedPrice = unitPrice;

				// 타임딜 적용
				if (isTimeDeal) {
					TimeDealItem timeDealItem = timeDealItemRepository
						.findActiveTimeDealItemByItemId(item.getId())
						.orElse(null);

					if (timeDealItem != null) {
						discountedPrice = timeDealItem.getPrice().intValue();
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
					// unitPrice,       // 정가, 현재는 할인 적용 변수에 정가를 반영 중이라 반대로 주석처리
					discountedPrice,     // TODO: 할인 가격 (FE 협의 후 노출)
					totalPrice,
					isTimeDeal,
					discountRatio
					// , isSoldOut        // TODO: 품절 여부 (FE 협의 후 노출)
				);
			})
			.toList();

		// 전체 수량 및 금액 집계
		int totalQuantity = itemResponses.stream()
			.mapToInt(CartItemResponse::getQuantity)
			.sum();

		int totalAmount = itemResponses.stream()
			.mapToInt(CartItemResponse::getTotalPrice)
			.sum();

		return new CartResponse(cart.getId(), itemResponses, totalQuantity, totalAmount);
	}
}