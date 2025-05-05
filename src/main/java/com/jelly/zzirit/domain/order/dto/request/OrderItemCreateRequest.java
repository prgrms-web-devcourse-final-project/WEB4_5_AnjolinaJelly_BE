package com.jelly.zzirit.domain.order.dto.request;

public record OrderItemCreateRequest(
    Long itemId,
    int quantity
) {
}
