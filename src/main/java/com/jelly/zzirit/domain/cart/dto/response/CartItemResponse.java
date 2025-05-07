package com.jelly.zzirit.domain.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "장바구니 상품 응답 DTO")
public class CartItemResponse {

	@Schema(description = "장바구니 아이템 ID", example = "101")
	private Long cartItemId;

	@Schema(description = "상품 ID", example = "5")
	private Long itemId;

	@Schema(description = "상품명", example = "iPhone 15")
	private String itemName;

	@Schema(description = "상품 종류", example = "스마트폰")
	private String type;

	@Schema(description = "브랜드명", example = "Apple")
	private String brand;

	@Schema(description = "수량", example = "2")
	private int quantity;

	@Schema(description = "상품 이미지 URL", example = "https://dummyimage.com/iphone.jpg")
	private String imageUrl;

	@Schema(description = "상품 정가", example = "1500000")
	private int originalPrice;

	@Schema(description = "할인 적용된 가격", example = "1350000")
	private int discountedPrice;

	@Schema(description = "총 가격 (수량 * 할인 가격)", example = "2700000")
	private int totalPrice;

	@Schema(description = "타임딜 상품 여부", example = "true")
	private boolean isTimeDeal;

	@Schema(description = "할인율", example = "10")
	private Integer discountRatio;

	@Schema(description = "품절 여부", example = "false")
	private boolean isSoldOut;
}