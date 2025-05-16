package com.jelly.zzirit.domain.order.repository.order;

import com.jelly.zzirit.domain.order.entity.Order;

public interface OrderRepositoryCustom {

	Order getUnconfirmedOrThrow(String orderNumber);
}