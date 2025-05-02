package com.jelly.zzirit.domain.order.dto.request;

import com.jelly.zzirit.domain.orderitem.dto.request.OrderItemCreateRequest;

import java.util.List;

public record OrderCreateRequest(
    String shippingRequest, // 배송 요청 사항
    List<OrderItemCreateRequest> items
) {
}
