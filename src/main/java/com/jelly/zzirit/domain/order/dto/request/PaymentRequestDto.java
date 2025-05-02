package com.jelly.zzirit.domain.order.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record PaymentRequestDto(
	List<OrderItemRequestDto> orderItems,
	BigDecimal totalAmount,
	String shippingAddress,
	String shippingAddressDetail
) {}