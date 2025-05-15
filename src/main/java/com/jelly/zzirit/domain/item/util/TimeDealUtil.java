package com.jelly.zzirit.domain.item.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TimeDealUtil {

	// 할인된 가격 계산 메서드
	public static BigDecimal calculateDiscountedPrice(BigDecimal originalPrice, int discountRate) {
		BigDecimal discount = BigDecimal.valueOf(discountRate).divide(BigDecimal.valueOf(100));
		return originalPrice.multiply(BigDecimal.ONE.subtract(discount));
	}

	// 기간 겹침 여부 판단 메서드
	public static boolean isTimeRangeOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2,
		LocalDateTime end2) {
		return !(end1.isBefore(start2) || start1.isAfter(end2));
	}

	// 타임딜 시작 시점 과거 여부 판단 메서드
	public static boolean isStartTimeInPast(LocalDateTime startTime, LocalDateTime referenceTime) {
		return startTime.isBefore(referenceTime);
	}
}
