package com.jelly.zzirit.domain.cart.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.cart.dto.response.CartItemResponse;
import com.jelly.zzirit.domain.cart.dto.response.CartResponse;
import com.jelly.zzirit.global.dto.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "장바구니 API", description = "장바구니와 관련된 API를 설명합니다.")
public class CartController {

	@Operation(
		summary = "내 장바구니 조회",
		description = "현재 로그인된 사용자의 장바구니를 조회합니다.",
		security = {@SecurityRequirement(name = "bearer")},
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "장바구니 조회 성공",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = CartResponse.class),
					examples = @ExampleObject(name = "성공 응답", value = """
							{
							  "success": true,
							  "code": 1000,
							  "httpStatus": 200,
							  "message": "요청이 성공하였습니다.",
							  "result": {
							    "cartId": 1001,
							    "items": [
							      {
							        "cartItemId": 1,
							        "itemId": 5,
							        "itemName": "iPhone 15 Pro",
							        "itemImageUrl": "https://dummyimage.com/iphone.jpg",
							        "quantity": 2,
							        "unitPrice": 1740000,
							        "totalPrice": 3480000,
							        "discountRatio": null,
							        "timeDeal": false
							      },
							      {
							        "cartItemId": 2,
							        "itemId": 9,
							        "itemName": "갤럭시북 타임딜 한정",
							        "itemImageUrl": "https://dummyimage.com/galaxybook.jpg",
							        "quantity": 1,
							        "unitPrice": 1390000,
							        "totalPrice": 1390000,
							        "discountRatio": 30,
							        "timeDeal": true
							      }
							    ],
							    "totalQuantity": 3,
							    "totalAmount": 4870000
							  }
							}
						""")
				)
			),
			@ApiResponse(
				responseCode = "400",
				description = "잘못된 요청 (예: 토큰 누락)",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = BaseResponse.class),
					examples = @ExampleObject(name = "잘못된 요청", value = """
							{
							  "success": false,
							  "code": 40012,
							  "httpStatus": 401,
							  "message": "토큰이 존재하지 않습니다",
							  "result": {}
							}
						""")
				)
			),
			@ApiResponse(
				responseCode = "401",
				description = "인증 실패 (토큰 없음/만료/잘못됨 등 상황에 따라 응답이 달라질 수 있음)",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = BaseResponse.class),
					examples = @ExampleObject(name = "인증 실패 예시", value = """
						 {
						   "success": false,
						   "code": 40002,
						   "httpStatus": 401,
						   "message": "인증되지 않은 요청입니다.",
						   "result": {}
						 }
						""")
				)
			)
		}
	)
	@GetMapping("/me")
	public BaseResponse<CartResponse> getMyCart() {
		// 하드코딩된 더미 응답 반환
		List<CartItemResponse> dummyItems = List.of(
			new CartItemResponse(1L, 5L, "iPhone 15 Pro", "https://dummyimage.com/iphone.jpg",
				2, 1740000, 3480000, false, null),
			new CartItemResponse(2L, 9L, "갤럭시북 타임딜 한정", "https://dummyimage.com/galaxybook.jpg",
				1, 1390000, 1390000, true, 30)
		);
		CartResponse response = new CartResponse(1001L, dummyItems, 3, 4870000);
		return BaseResponse.success(response);
	}
}