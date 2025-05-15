package com.jelly.zzirit.domain.order.service.pay;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.domain.order.dto.request.PaymentRequest;
import com.jelly.zzirit.domain.order.dto.response.PaymentInitResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.service.order.CommandOrderSequence;
import com.jelly.zzirit.domain.order.service.order.CommandTempOrderService;
import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentInitService {

	private final CommandOrderSequence orderSequenceGenerator;
	private final CommandTempOrderService tempOrderService;
	private final MemberRepository memberRepository;

	public PaymentInitResponse createOrderAndReturnInit(PaymentRequest dto) {
		Long todaySequence = orderSequenceGenerator.getTodaySequence();
		String orderNumber = Order.generateOrderNumber(todaySequence);

		Member member = memberRepository.findById( AuthMember.getMemberId())
			.orElseThrow(() -> new InvalidUserException(BaseResponseStatus.USER_NOT_FOUND));

		Order order = tempOrderService.createTempOrder(dto, member, orderNumber);

		return PaymentInitResponse.builder()
			.orderId(order.getOrderNumber())
			.amount(dto.totalAmount())
			.orderName(
				dto.orderItems().size() > 1
					? dto.orderItems().getFirst().itemName() + " 외 " + (dto.orderItems().size() - 1) + "건"
					: dto.orderItems().getFirst().itemName()
			)
			.customerName(member.getMemberName())
			.build();
	}
}