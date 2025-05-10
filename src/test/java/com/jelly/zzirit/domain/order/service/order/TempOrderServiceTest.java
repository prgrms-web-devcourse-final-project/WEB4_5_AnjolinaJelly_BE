package com.jelly.zzirit.domain.order.service.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.dto.request.OrderItemRequestDto;
import com.jelly.zzirit.domain.order.dto.request.PaymentRequestDto;
import com.jelly.zzirit.domain.order.dto.response.TossPaymentResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.mapper.OrderMapper;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

@ExtendWith(MockitoExtension.class)
class TempOrderServiceTest {

	@InjectMocks
	private TempOrderService tempOrderService;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private OrderService orderService;

	@Mock
	private OrderMapper orderMapper;

	@Test
	void 정상적으로_임시주문을_생성한다() {
		// given
		Member member = mock(Member.class);
		PaymentRequestDto dto = new PaymentRequestDto(
			List.of(new OrderItemRequestDto(1L, 2, "샘플", new BigDecimal("10000"))),
			new BigDecimal("20000"), "요청사항", "서울", "101동"
		);
		Order order = mock(Order.class);

		when(orderMapper.mapToTempOrder(dto, member, "ORDER-001")).thenReturn(order);
		doNothing().when(orderMapper).mapToOrderItems(order, dto.orderItems());

		// when
		Order result = tempOrderService.createTempOrder(dto, member, "ORDER-001");

		// then
		assertNotNull(result);
		verify(orderMapper).mapToTempOrder(dto, member, "ORDER-001");
		verify(orderMapper).mapToOrderItems(order, dto.orderItems());
		verify(orderRepository).save(order);
	}

	@Test
	void 존재하지_않는_아이템일_경우_예외() {
		// given
		Member member = mock(Member.class);
		PaymentRequestDto dto = new PaymentRequestDto(
			List.of(new OrderItemRequestDto(99L, 1, "없는상품", new BigDecimal("10000"))),
			new BigDecimal("10000"), null, null, null
		);
		Order order = mock(Order.class);
		when(orderMapper.mapToTempOrder(dto, member, "ORDER-999")).thenReturn(order);

		doThrow(new InvalidOrderException(BaseResponseStatus.ITEM_NOT_FOUND))
			.when(orderMapper).mapToOrderItems(any(), any());

		// when & then
		assertThrows(InvalidOrderException.class, () ->
			tempOrderService.createTempOrder(dto, member, "ORDER-999"));
	}


	@Test
	void 정상적으로_주문확정을_진행한다() {
		// given
		Order order = mock(Order.class);
		TossPaymentResponse response = mock(TossPaymentResponse.class);

		when(response.getOrderId()).thenReturn("ORDER-001");
		when(response.getPaymentKey()).thenReturn("pay_123");
		when(response.getTotalAmount()).thenReturn(new BigDecimal("10000"));
		when(response.getStatus()).thenReturn("DONE");
		when(response.getMethod()).thenReturn("카드");

		when(orderRepository.findByOrderNumber("ORDER-001")).thenReturn(Optional.of(order));
		when(order.getTotalPrice()).thenReturn(new BigDecimal("10000"));
		when(order.getOrderNumber()).thenReturn("ORDER-001");
		when(order.getStatus()).thenReturn(Order.OrderStatus.PENDING);

		// when
		tempOrderService.confirmTempOrder(response);

		// then
		verify(paymentRepository).save(any());
		verify(orderService).completeOrder(order, "pay_123");
	}

	@Test
	void 주문이_없으면_예외가_발생한다() {
		TossPaymentResponse response = mock(TossPaymentResponse.class);
		when(response.getOrderId()).thenReturn("ORDER-404");

		when(orderRepository.findByOrderNumber("ORDER-404")).thenReturn(Optional.empty());

		assertThrows(InvalidOrderException.class, () ->
			tempOrderService.confirmTempOrder(response));
	}

	@Test
	void 임시주문이_성공적으로_삭제된다() {
		Order mockOrder = mock(Order.class);

		when(orderRepository.findByOrderNumber("ORDER-001")).thenReturn(Optional.of(mockOrder));
		when(mockOrder.isConfirmed()).thenReturn(false);
		when(paymentRepository.findByOrder(mockOrder)).thenReturn(Optional.empty());

		tempOrderService.deleteTempOrder("ORDER-001");

		verify(paymentRepository).findByOrder(mockOrder);
		verify(orderRepository).delete(mockOrder);
	}

	@Test
	void 삭제하려는_임시주문이_존재하지_않으면_예외() {
		when(orderRepository.findByOrderNumber("ORDER-404")).thenReturn(Optional.empty());

		assertThrows(InvalidOrderException.class, () ->
			tempOrderService.deleteTempOrder("ORDER-404"));
	}

	@Test
	void 이미_확정된_주문이면_삭제시_예외발생() {
		Order confirmedOrder = mock(Order.class);

		when(orderRepository.findByOrderNumber("ORDER-002")).thenReturn(Optional.of(confirmedOrder));
		when(confirmedOrder.isConfirmed()).thenReturn(true);

		assertThrows(InvalidOrderException.class, () ->
			tempOrderService.deleteTempOrder("ORDER-002"));
	}
}