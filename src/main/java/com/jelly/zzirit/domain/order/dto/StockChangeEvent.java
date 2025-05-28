package com.jelly.zzirit.domain.order.dto;

import java.time.LocalDateTime;

public record StockChangeEvent(
	Long itemId,
	String orderNumber,
	String action,
	int quantity,
	LocalDateTime timestamp
) {

	public static StockChangeEvent decrease(Long itemId, String orderNumber, int quantity) {
		return new StockChangeEvent(
			itemId,
			orderNumber,
			"DECREASE",
			quantity,
			LocalDateTime.now()
		);
	}

	public static StockChangeEvent restore(Long itemId, String orderNumber, int quantity) {
		return new StockChangeEvent(
			itemId,
			orderNumber,
			"RESTORE",
			quantity,
			LocalDateTime.now()
		);
	}

	public static StockChangeEvent refund(String orderNumber, int quantity) {
		return new StockChangeEvent(
			null,
			orderNumber,
			"REFUND",
			quantity,
			LocalDateTime.now()
		);
	}
}