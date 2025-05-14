package com.jelly.zzirit.domain.item.util;

import java.math.BigDecimal;

public class TimeDealUtil {

	// 할인된 가격 계산 메서드
	public static BigDecimal calculateDiscountedPrice(BigDecimal originalPrice, int discountRate) {
		BigDecimal discount = BigDecimal.valueOf(discountRate).divide(BigDecimal.valueOf(100));
		return originalPrice.multiply(BigDecimal.ONE.subtract(discount));
	}
}
