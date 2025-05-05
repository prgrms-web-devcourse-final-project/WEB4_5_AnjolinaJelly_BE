package com.jelly.zzirit.domain.cart.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.cart.dto.request.CartItemAddRequest;
import com.jelly.zzirit.domain.cart.dto.response.CartItemResponse;
import com.jelly.zzirit.domain.cart.service.CartItemService;
import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/cart/items")
@RequiredArgsConstructor
@Tag(name = "장바구니 API", description = "장바구니와 관련된 API를 설명합니다.")
public class CartItemController {

	private final CartItemService cartItemService;

	@Operation(
		summary = "장바구니에 상품 추가",
		description = "상품 ID와 수량을 전달받아 장바구니에 항목을 추가합니다.",
		security = {@SecurityRequirement(name = "bearer")}
	)
	@PostMapping
	public BaseResponse<CartItemResponse> addItemToCart(@Valid @RequestBody CartItemAddRequest request) {
		Long memberId = AuthMember.getMemberId();
		log.info("장바구니 추가 요청 - 사용자 ID: {}, itemId: {}", memberId, request.getItemId());

		CartItemResponse response = cartItemService.addItemToCart(memberId, request);
		return BaseResponse.success(response);
	}

	@Operation(
		summary = "장바구니 항목 삭제",
		description = "장바구니에서 항목을 제거합니다.",
		security = {@SecurityRequirement(name = "bearer")}
	)
	@DeleteMapping("/{itemId}")
	public BaseResponse<Empty> removeItemToCart(@PathVariable Long itemId) {
		Long memberId = AuthMember.getMemberId();
		log.info("장바구니에서 itemId {} 삭제 요청 - 사용자 ID: {}", itemId, memberId);

		cartItemService.removeItemToCart(memberId, itemId);
		return BaseResponse.success();
	}
}