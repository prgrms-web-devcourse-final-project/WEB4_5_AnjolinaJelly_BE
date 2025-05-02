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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/cart/items")
@RequiredArgsConstructor
@Tag(name = "cart", description = "장바구니 API")
public class CartItemController {

	@Operation(
		summary = "장바구니에 상품 추가",
		description = "상품 ID와 수량을 전달받아 장바구니에 항목을 추가합니다.",
		security = {@SecurityRequirement(name = "bearer")},
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "항목 추가 성공",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = CartItemResponse.class),
					examples = @ExampleObject(name = "성공 응답", value = """
							{
							  "success": true,
							  "code": 1000,
							  "httpStatus": 200,
							  "message": "요청이 성공하였습니다.",
							  "result": {
							    "cartItemId": 1,
							    "itemId": 9,
							    "itemName": "iPhone 15 Pro 256GB",
							    "itemImageUrl": "https://dummyimage.com/iphone15.jpg",
							    "quantity": 2,
							    "unitPrice": 1740000,
							    "totalPrice": 3480000,
							    "discountRatio": 10,
							    "timeDeal": true
							  }
							}
						""")
				)
			),
			@ApiResponse(
				responseCode = "400",
				description = "요청 값 오류 (예: 수량이 0)",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = BaseResponse.class),
					examples = @ExampleObject(name = "수량 오류", value = """
							{
							  "success": false,
							  "code": 40000,
							  "httpStatus": 400,
							  "message": "입력 값이 유효하지 않습니다",
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
	@PostMapping
	public BaseResponse<CartItemResponse> addItemToCart(@Valid @RequestBody CartItemAddRequest request) {
		Long memberId = AuthMember.getMemberId();
		log.info("장바구니 추가 요청 - 사용자 ID: {}, itemId: {}", memberId, request.getItemId());

		CartItemResponse response = switch (request.getItemId().intValue()) {
			case 9 -> new CartItemResponse(1L, 9L, "iPhone 15 Pro 256GB", "https://dummyimage.com/iphone15.jpg",
				request.getQuantity(), 1740000, 1740000 * request.getQuantity(), request.isTimeDeal(),
				request.isTimeDeal() ? 10 : null);
			default -> new CartItemResponse(99L, request.getItemId(), "샘플 상품", "https://dummyimage.com/sample.jpg",
				request.getQuantity(), 123456, 123456 * request.getQuantity(), request.isTimeDeal(), null);
		};

		return BaseResponse.success(response);
	}

	@Operation(
		summary = "장바구니 항목 삭제",
		description = "장바구니에서 항목을 제거합니다.",
		security = {@SecurityRequirement(name = "bearer")},
		responses = {
			@ApiResponse(
				responseCode = "200",
				description = "삭제 성공",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = Empty.class),
					examples = @ExampleObject(name = "성공 응답", value = """
							{
							  "success": true,
							  "code": 1000,
							  "httpStatus": 200,
							  "message": "요청이 성공하였습니다.",
							  "result": {}
							}
						""")
				)
			),
			@ApiResponse(
				responseCode = "401",
				description = "인증 실패",
				content = @Content(
					mediaType = "application/json",
					schema = @Schema(implementation = BaseResponse.class),
					examples = @ExampleObject(name = "JWT 없음", value = """
							{
							  "success": false,
							  "code": 40012,
							  "httpStatus": 401,
							  "message": "토큰이 존재하지 않습니다",
							  "result": {}
							}
						""")
				)
			)
		}
	)
	@DeleteMapping("/{itemId}")
	public BaseResponse<Empty> removeItemFromCart(@PathVariable Long itemId) {
		Long memberId = AuthMember.getMemberId();
		log.info("장바구니에서 itemId {} 삭제 요청 - 사용자 ID: {}", itemId, memberId);
		return BaseResponse.success();
	}
}