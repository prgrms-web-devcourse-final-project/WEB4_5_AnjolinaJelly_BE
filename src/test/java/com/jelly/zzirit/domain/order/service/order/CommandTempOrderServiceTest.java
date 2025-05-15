package com.jelly.zzirit.domain.order.service.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.dto.request.OrderItemCreateRequest;
import com.jelly.zzirit.domain.order.dto.request.PaymentRequest;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.mapper.OrderMapper;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

@ExtendWith(MockitoExtension.class)
class CommandTempOrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderMapper orderMapper;

	@InjectMocks
	private CommandTempOrderService commandTempOrderService;

	@Mock
	private PaymentRequest paymentRequest;

	@Mock
	private Member member;

	@Mock
	private Order order;

	@Test
	void createTempOrder_성공() {
		// given
		String orderNumber = "ORD20230501-000001";

		List<OrderItemCreateRequest> orderItems = new ArrayList<>();
		orderItems.add(new OrderItemCreateRequest(1L, "테스트 상품",2));
		given(paymentRequest.orderItems()).willReturn(orderItems);

		given(orderMapper.mapToTempOrder(paymentRequest, member, orderNumber)).willReturn(order);
		doNothing().when(orderMapper).mapToOrderItems(order, orderItems);
		given(orderRepository.save(order)).willReturn(order);

		// when
		Order createdOrder = commandTempOrderService.createTempOrder(paymentRequest, member, orderNumber);

		// then
		assertNotNull(createdOrder);
		verify(orderRepository).save(order);
	}

	@Test
	void deleteTempOrder_성공() {
		// given
		String orderId = "ORD20230501-000001";
		given(orderRepository.findByOrderNumber(orderId)).willReturn(Optional.of(order));
		given(order.isConfirmed()).willReturn(false);

		// when
		commandTempOrderService.deleteTempOrder(orderId);

		// then
		verify(orderRepository).delete(order);
	}

	@Test
	void deleteTempOrder_이미_처리된_주문() {
		// given
		String orderId = "ORD20230501-000001";
		given(orderRepository.findByOrderNumber(orderId)).willReturn(Optional.of(order));
		given(order.isConfirmed()).willReturn(true);

		// when, then
		assertThrows(InvalidOrderException.class, () -> commandTempOrderService.deleteTempOrder(orderId));
	}
}