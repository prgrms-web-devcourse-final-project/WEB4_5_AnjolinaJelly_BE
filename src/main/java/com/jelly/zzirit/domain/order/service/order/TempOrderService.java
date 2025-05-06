package com.jelly.zzirit.domain.order.service.order;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.item.entity.Item;
import com.jelly.zzirit.domain.item.repository.ItemRepository;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.dto.request.OrderItemRequestDto;
import com.jelly.zzirit.domain.order.dto.request.PaymentRequestDto;
import com.jelly.zzirit.domain.order.dto.response.TossPaymentResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.OrderItem;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.repository.OrderItemRepository;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.domain.order.service.pay.TossPaymentValidation;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TempOrderService {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final ItemRepository itemRepository;
	private final PaymentRepository paymentRepository;
	private final OrderService orderService;

	@Transactional
	public Order createTempOrder(PaymentRequestDto dto, Member member, String orderNumber) {

		Order order = Order.tempOf(member, orderNumber, dto);
		orderRepository.save(order);

		List<Long> itemIds = dto.orderItems().stream()
			.map(OrderItemRequestDto::itemId)
			.toList();

		Map<Long, Item> itemMap = itemRepository.findAllById(itemIds).stream()
			.collect(Collectors.toMap(Item::getId, Function.identity()));

		List<OrderItem> orderItems = dto.orderItems().stream()
			.map(itemDto -> {
				Item item = Optional.ofNullable(itemMap.get(itemDto.itemId()))
					.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ITEM_NOT_FOUND));

				OrderItem orderItem = OrderItem.of(order, item, itemDto.quantity(), itemDto.price());
				order.addOrderItem(orderItem);
				return orderItem;
			})
			.toList();

		orderItemRepository.saveAll(orderItems);

		return order;
	}

	@Transactional
	public void confirmTempOrder(
		String paymentKey,
		String orderNumber,
		String amount,
		TossPaymentResponse paymentInfo
	) {
		Order order = orderRepository.findByOrderNumber(orderNumber)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ORDER_NOT_FOUND));

		TossPaymentValidation.validateAll(order, paymentInfo, amount);

		Payment payment = Payment.of(order, paymentInfo.getPaymentKey(), paymentInfo.getMethod());
		paymentRepository.save(payment);

		orderService.completeOrder(order, paymentKey);
	}
}