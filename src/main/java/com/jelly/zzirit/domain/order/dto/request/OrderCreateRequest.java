package com.jelly.zzirit.domain.order.dto.request;

import java.util.List;

public record OrderCreateRequest(
    String shippingRequest, // 배송 요청 사항
    List<OrderItemCreateRequest> items
) {
}
