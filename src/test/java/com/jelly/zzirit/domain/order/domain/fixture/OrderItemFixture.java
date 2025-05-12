package com.jelly.zzirit.domain.order.domain.fixture;

import java.math.BigDecimal;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.OrderItem;

public class OrderItemFixture {
    public static OrderItem 주문_상품_생성(Order order, Item item) {
        return OrderItem.of(order, item, 2, BigDecimal.valueOf(5000));
    }
}
