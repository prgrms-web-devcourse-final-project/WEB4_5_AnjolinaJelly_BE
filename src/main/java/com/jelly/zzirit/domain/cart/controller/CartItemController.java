package com.jelly.zzirit.domain.cart.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.cart.dto.request.CartItemAddRequest;
import com.jelly.zzirit.domain.cart.dto.response.CartItemResponse;
import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/cart/items")
@RequiredArgsConstructor
public class CartItemController {

	@PostMapping
	public BaseResponse<CartItemResponse> addItemToCart(@RequestBody CartItemAddRequest request) {
		Long memberId = AuthMember.getMemberId();
		log.info("장바구니 추가 요청 - 사용자 ID: {}, itemId: {}", memberId, request.getItemId());

		CartItemResponse response = switch (request.getItemId().intValue()) {
			case 9 -> new CartItemResponse(1L, 9L, "iPhone 15 Pro 256GB", "https://dummyimage.com/iphone15.jpg",
				request.getQuantity(), 1740000, 1740000 * request.getQuantity(), request.isTimeDeal(), request.isTimeDeal() ? 10 : null);
			case 10 -> new CartItemResponse(2L, 10L, "갤럭시 S24 Ultra", "https://dummyimage.com/galaxy-s24.jpg",
				request.getQuantity(), 1570000, 1570000 * request.getQuantity(), request.isTimeDeal(), request.isTimeDeal() ? 20 : null);
			case 5 -> new CartItemResponse(3L, 5L, "삼성 비스포크 냉장고 815L", "https://dummyimage.com/samsung-fridge.jpg",
				request.getQuantity(), 2630000, 2630000 * request.getQuantity(), request.isTimeDeal(), request.isTimeDeal() ? 15 : null);
			default -> new CartItemResponse(99L, request.getItemId(), "하드코딩 상품명", "https://dummyimage.com/sample.jpg",
				request.getQuantity(), 123456, 123456 * request.getQuantity(), request.isTimeDeal(), request.isTimeDeal() ? 5 : null);
		};

		return BaseResponse.success(response);
	}

	@DeleteMapping("/{itemId}")
	public BaseResponse<Empty> removeItemFromCart(@PathVariable Long itemId) {
		Long memberId = AuthMember.getMemberId();
		log.info("장바구니에서 itemId {} 삭제 요청 - 사용자 ID: {}", itemId, memberId);
		return BaseResponse.success();
	}
}
