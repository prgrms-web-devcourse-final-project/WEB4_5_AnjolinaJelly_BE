package com.jelly.zzirit.domain.order.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.jelly.zzirit.domain.order.util.PaymentProvider;

public record PaymentRequestDto(
	List<OrderItemRequestDto> orderItems,
	BigDecimal totalAmount,
	String shippingRequest,
	String address,
	String addressDetail,
	PaymentProvider provider
) {}