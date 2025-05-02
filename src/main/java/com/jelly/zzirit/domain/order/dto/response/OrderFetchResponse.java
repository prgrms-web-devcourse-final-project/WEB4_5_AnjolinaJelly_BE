package com.jelly.zzirit.domain.order.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.orderitem.dto.response.OrderItemFetchResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderFetchResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm:ss")
    LocalDateTime orderDate, // 주문 날짜
        Long orderId, // 주문 아이디 // 주문 취소 시 필요
        String orderNumber, // 주문 번호
        BigDecimal totalPrice, // 총 주문 금액 // (개당 가격 * 개수)의 합
        Order.OrderStatus orderStatus, // 주문 상태
        List<OrderItemFetchResponse> items // 해당 주문에 포함된 상품 데이터
) {
}
