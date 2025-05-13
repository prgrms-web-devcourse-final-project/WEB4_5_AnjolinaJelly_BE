package com.jelly.zzirit.domain.order.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record PaymentRequest(
	List<OrderItemCreateRequest> orderItems,
	BigDecimal totalAmount,
	String shippingRequest,
	String address,
	String addressDetail
) {}