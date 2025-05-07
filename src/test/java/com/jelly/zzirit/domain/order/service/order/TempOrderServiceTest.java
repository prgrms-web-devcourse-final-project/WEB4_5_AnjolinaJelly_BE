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
import com.jelly.zzirit.domain.order.repository.OrderItemRepository;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

@ExtendWith(MockitoExtension.class)
class TempOrderServiceTest {

	@InjectMocks
	private TempOrderService tempOrderService;

	@Mock
	private OrderRepository orderRepository;
	@Mock private OrderItemRepository orderItemRepository;
	@Mock private ItemRepository itemRepository;
	@Mock private PaymentRepository paymentRepository;
	@Mock private OrderService orderService;

	@Test
	void 정상적으로_임시주문을_생성한다() {
		// given
		Member member = mock(Member.class);
		PaymentRequestDto dto = new PaymentRequestDto(
			List.of(new OrderItemRequestDto(1L, null, 2, "샘플", new BigDecimal("10000"))),
			new BigDecimal("20000"),
			"요청사항",
			"서울",
			"101동"
		);
		Item item = Item.builder().id(1L).build();

		when(itemRepository.findAllById(anyList())).thenReturn(List.of(item));

		// when
		Order order = tempOrderService.createTempOrder(dto, member, "ORDER-001");

		// then
		assertNotNull(order);
		verify(orderRepository).save(any());
		verify(orderItemRepository).saveAll(any());
	}

	@Test
	void 존재하지_않는_아이템일_경우_예외() {
		// given
		Member member = mock(Member.class);
		PaymentRequestDto dto = new PaymentRequestDto(
			List.of(new OrderItemRequestDto(99L, null, 1, "없는상품", new BigDecimal("10000"))),
			new BigDecimal("10000"),
			null,
			null,
			null
		);
		when(itemRepository.findAllById(anyList())).thenReturn(List.of());

		// when & then
		assertThrows(InvalidOrderException.class, () ->
			tempOrderService.createTempOrder(dto, member, "ORDER-999"));
	}

	@Test
	void 정상적으로_주문확정을_진행한다() {
		// given
		Order order = mock(Order.class);
		TossPaymentResponse response = mock(TossPaymentResponse.class);

		when(orderRepository.findByOrderNumber("ORDER-001")).thenReturn(Optional.of(order));
		when(order.getTotalPrice()).thenReturn(new BigDecimal("10000"));
		when(order.getOrderNumber()).thenReturn("ORDER-001");
		when(order.getStatus()).thenReturn(Order.OrderStatus.PENDING);
		when(response.getStatus()).thenReturn("DONE");
		when(response.getTotalAmount()).thenReturn(new BigDecimal("10000"));
		when(response.getPaymentKey()).thenReturn("pay_123");
		when(response.getMethod()).thenReturn("카드");
		when(response.getOrderId()).thenReturn("ORDER-001");

		// when
		tempOrderService.confirmTempOrder("pay_123", "ORDER-001", "10000", response);

		// then
		verify(paymentRepository).save(any());
		verify(orderService).completeOrder(order, "pay_123");
	}


	@Test
	void 주문이_없으면_예외가_발생한다() {
		when(orderRepository.findByOrderNumber(anyString())).thenReturn(Optional.empty());

		assertThrows(InvalidOrderException.class, () ->
			tempOrderService.confirmTempOrder("pay_xxx", "ORDER-404", "10000", mock(TossPaymentResponse.class)));
	}

	@Test
	void 임시주문이_성공적으로_삭제된다() {
		Order mockOrder = mock(Order.class);

		when(orderRepository.findByOrderNumber("ORDER-001")).thenReturn(Optional.of(mockOrder));
		when(mockOrder.isConfirmed()).thenReturn(false);
		when(paymentRepository.findByOrder(mockOrder)).thenReturn(Optional.empty());

		tempOrderService.deleteTempOrder("ORDER-001", "USER_CANCEL", "사용자 취소");

		verify(paymentRepository).findByOrder(mockOrder);
		verify(orderRepository).delete(mockOrder);
	}

	@Test
	void 삭제하려는_임시주문이_존재하지_않으면_예외() {
		when(orderRepository.findByOrderNumber("ORDER-404")).thenReturn(Optional.empty());

		assertThrows(InvalidOrderException.class, () ->
			tempOrderService.deleteTempOrder("ORDER-404", "NOT_FOUND", "존재하지 않는 주문"));
	}

	@Test
	void 이미_확정된_주문이면_삭제시_예외발생() {
		Order confirmedOrder = mock(Order.class);

		when(orderRepository.findByOrderNumber("ORDER-002")).thenReturn(Optional.of(confirmedOrder));
		when(confirmedOrder.isConfirmed()).thenReturn(true);

		assertThrows(InvalidOrderException.class, () ->
			tempOrderService.deleteTempOrder("ORDER-002", "ALREADY_CONFIRMED", "이미 확정된 주문"));
	}
}