package com.jelly.zzirit.domain.cart.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.cart.dto.request.CartItemCreateRequest;
import com.jelly.zzirit.domain.cart.dto.response.CartItemFetchResponse;
import com.jelly.zzirit.domain.cart.service.CartItemService;
import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart/items")
@RequiredArgsConstructor
@Tag(name = "장바구니 API", description = "장바구니와 관련된 API를 설명합니다.")
public class CartItemController {

	private final CartItemService cartItemService;

	@Operation(summary = "장바구니에 상품 추가", description = "상품 ID와 수량을 전달받아 장바구니에 항목을 추가합니다.")
	@PostMapping
	public BaseResponse<CartItemFetchResponse> addItemToCart(@Valid @RequestBody CartItemCreateRequest request) {
		return BaseResponse.success(
			cartItemService.addItemToCart(AuthMember.getMemberId(), request)
		);
	}

	@Operation(summary = "장바구니 항목 삭제", description = "장바구니에서 항목을 제거합니다.")
	@DeleteMapping("/{item-id}")
	public BaseResponse<Empty> removeItemToCart(@PathVariable("item-id") Long itemId) {
		cartItemService.removeItemToCart(AuthMember.getMemberId(), itemId);
		return BaseResponse.success();
	}

	@Operation(summary = "장바구니 상품 수량 증가", description = "수량 1 증가")
	@PostMapping("/{item-id}/increase")
	public BaseResponse<CartItemFetchResponse> increaseQuantity(@PathVariable("item-id") Long itemId) {
		return BaseResponse.success(
			cartItemService.modifyQuantity(AuthMember.getMemberId(), itemId, +1)
		);
	}

	@Operation(summary = "장바구니 상품 수량 감소", description = "수량 1 감소")
	@PostMapping("/{item-id}/decrease")
	public BaseResponse<CartItemFetchResponse> decreaseQuantity(@PathVariable("item-id") Long itemId) {
		return BaseResponse.success(
			cartItemService.modifyQuantity(AuthMember.getMemberId(), itemId, -1)
		);
	}
}