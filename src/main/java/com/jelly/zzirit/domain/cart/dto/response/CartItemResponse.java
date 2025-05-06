package com.jelly.zzirit.domain.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartItemResponse {
	private Long cartItemId;
	private Long itemId;
	private String itemName;
	private String itemImageUrl;
	private int quantity;
	private int unitPrice;          // 상품 정가

	// private int discountPrice;   // TODO: 타임딜 등 할인 적용가 (FE 협의 후 주석 제거)

	private int totalPrice;         // 수량 * 할인 단가
	private boolean isTimeDeal;
	private Integer discountRatio;  // 할인율

	// private boolean isSoldOut;   // TODO: 재고 0 여부 (FE 협의 후 주석 제거)
}
