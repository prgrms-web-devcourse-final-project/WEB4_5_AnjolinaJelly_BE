package com.jelly.zzirit.domain.order.service.order;

import static org.mockito.Mockito.*;

import java.util.List;

import com.jelly.zzirit.domain.order.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.OrderItem;

@ExtendWith(MockitoExtension.class)
class OrderManagerTest {

	@InjectMocks
	private OrderManager orderManager;

	@Mock
	private ItemStockService itemStockService;

	@Test
	void 재고차감_및_주문완료처리_정상작동() {
		// given
		Item item1 = Item.builder().id(1L).build();
		Item item2 = Item.builder().id(2L).build();

		OrderItem orderItem1 = OrderItem.builder()
			.item(item1)
			.quantity(2)
			.build();

		OrderItem orderItem2 = OrderItem.builder()
			.item(item2)
			.quantity(3)
			.build();

		Order order = Mockito.spy(Order.builder()
			.orderItems(List.of(orderItem1, orderItem2))
			.build());

		// when
		orderManager.process(order);

		// then
		verify(itemStockService).decrease(1L, 2);
		verify(itemStockService).decrease(2L, 3);
		verify(order).changeStatus(OrderStatus.COMPLETED);
	}
}
