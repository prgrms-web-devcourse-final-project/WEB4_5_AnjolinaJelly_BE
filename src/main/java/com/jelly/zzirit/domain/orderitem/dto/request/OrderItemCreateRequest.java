package com.jelly.zzirit.domain.orderitem.dto.request;

public record OrderItemCreateRequest(
    Long itemId,
    int quantity
) {
}
