package com.jelly.zzirit.domain.orderitem.dto.response;

import com.jelly.zzirit.domain.order.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemFetchResponse(
    String itemName,
    int quantity,
    String imageUrl,
    BigDecimal totalPrice // 이 상품에 대한 총 주문 금액 // 개당 가격 * 개수
) {
    public static OrderItemFetchResponse from(OrderItem orderItem) {
        return new OrderItemFetchResponse(
            orderItem.getItem().getName(),
            orderItem.getQuantity(),
            orderItem.getItem().getImageUrl(),
            orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()))
        );
    }
}
