package com.jelly.zzirit.domain.cart.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.cart.dto.response.CartItemResponse;
import com.jelly.zzirit.domain.cart.dto.response.CartResponse;
import com.jelly.zzirit.global.dto.BaseResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

	@GetMapping("/me")
	public BaseResponse<CartResponse> getMyCart() {

		// 하드코딩된 더미 데이터
		List<CartItemResponse> dummyItems = List.of(
			new CartItemResponse(
				1L, 5L, "iPhone 15 Pro", "https://dummyimage.com/iphone.jpg",
				2, 1740000, 3480000, false, null // 일반 상품
			),
			new CartItemResponse(
				2L, 9L, "갤럭시북 타임딜 한정", "https://dummyimage.com/galaxybook.jpg",
				1, 1390000, 1390000, true, 30 // 타임딜 상품
			)
		);

		CartResponse response = new CartResponse(
			1001L,
			dummyItems,
			3,
			4870000
		);

		return BaseResponse.success(response);
	}
}