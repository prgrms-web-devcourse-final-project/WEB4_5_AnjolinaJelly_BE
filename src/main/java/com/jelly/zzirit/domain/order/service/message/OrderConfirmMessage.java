package com.jelly.zzirit.domain.order.service.message;

import java.util.List;

import com.jelly.zzirit.domain.order.entity.Order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderConfirmMessage {

	private String orderNumber;
	private String paymentKey;
	private String amount;
	private String method;

	private List<ItemPayload> items;

	public static OrderConfirmMessage from(Order order, String paymentKey, String amount, String method) {
		return new OrderConfirmMessage(
				order.getOrderNumber(),
				paymentKey,
				amount,
				method,
				order.getOrderItems().stream()
						.map(item -> new ItemPayload(item.getItem().getId(), item.getQuantity()))
						.toList()
		);
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ItemPayload {
		private Long itemId;
		private int quantity;
	}
}