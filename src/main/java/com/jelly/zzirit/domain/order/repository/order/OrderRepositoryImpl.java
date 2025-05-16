package com.jelly.zzirit.domain.order.repository.order;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.OrderStatus;
import com.jelly.zzirit.domain.order.entity.QOrder;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final QOrder order = QOrder.order;

	@Override
	public Order getUnconfirmedOrThrow(String orderNumber) {
		Order result = queryFactory.selectFrom(order)
			.where(
				order.orderNumber.eq(orderNumber),
				order.status.notIn(OrderStatus.PAID, OrderStatus.COMPLETED)
			)
			.fetchOne();

		if (result == null) {
			throw new InvalidOrderException(BaseResponseStatus.ALREADY_PROCESSED);
		}

		return result;
	}
}