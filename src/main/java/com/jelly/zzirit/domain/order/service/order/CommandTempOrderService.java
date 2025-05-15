package com.jelly.zzirit.domain.order.service.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.dto.request.PaymentRequest;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.mapper.OrderMapper;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandTempOrderService {

	private final OrderRepository orderRepository;
	private final OrderMapper orderMapper;

	@Transactional
	public Order createTempOrder(PaymentRequest dto, Member member, String orderNumber) {
		Order tempOrder = orderMapper.mapToTempOrder(dto, member, orderNumber);
		orderMapper.mapToOrderItems(tempOrder, dto.orderItems());
		orderRepository.save(tempOrder);

		return tempOrder;
	} //임시 주문 생성

	@Transactional
	public void deleteTempOrder(String orderId) {
		Order order = orderRepository.findByOrderNumber(orderId)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ORDER_NOT_FOUND));

		if (order.isConfirmed()) {
			throw new InvalidOrderException(BaseResponseStatus.ALREADY_PROCESSED);
		}

		orderRepository.delete(order);
	} // 결제 실패 또는 취소 시 임시 주문 제거
}