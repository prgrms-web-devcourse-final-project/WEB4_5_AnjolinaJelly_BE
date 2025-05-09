package com.jelly.zzirit.domain.order.dto.request;

public record OrderItemRequestDto(
	Long itemId,
	int quantity
) {}