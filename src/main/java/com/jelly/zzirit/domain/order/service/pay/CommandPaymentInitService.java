package com.jelly.zzirit.domain.order.service.pay;

import com.jelly.zzirit.domain.order.util.CircuitBreakerRecoveryTrigger;
import com.jelly.zzirit.domain.order.util.CircuitBreakerStatusChecker;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.domain.order.dto.request.PaymentRequest;
import com.jelly.zzirit.domain.order.dto.response.PaymentInitResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.service.order.manage.CommandOrderSequence;
import com.jelly.zzirit.domain.order.service.order.manage.CommandTempOrderService;
import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandPaymentInitService {

	private final CommandOrderSequence orderSequenceGenerator;
	private final CommandTempOrderService tempOrderService;
	private final CircuitBreakerStatusChecker breakerChecker;
	private final CircuitBreakerRecoveryTrigger circuitBreakerRecoveryTrigger;
	private final MemberRepository memberRepository;

	public PaymentInitResponse createOrderAndReturnInit(PaymentRequest dto) {

		if (breakerChecker.isCircuitOpen("tossPaymentConfirmBreaker")) {
			log.warn("CircuitBreaker OPEN 상태 → 결제 시도 차단");
			throw new InvalidOrderException(BaseResponseStatus.CIRCUIT_BREAKER_OPEN);
		}

		if (breakerChecker.isCircuitHalfOpen("tossPaymentConfirmBreaker")) {
			log.info("CircuitBreaker HALF_OPEN 상태 → 복구 시도 실행");
			circuitBreakerRecoveryTrigger.attemptRecovery();
		}

		Member member = memberRepository.findById(AuthMember.getMemberId())
			.orElseThrow(() -> new InvalidUserException(BaseResponseStatus.USER_NOT_FOUND));

		Long todaySequence = orderSequenceGenerator.getTodaySequence();
		String orderNumber = Order.generateOrderNumber(todaySequence);

		Order order = tempOrderService.createTempOrder(dto, member, orderNumber);

		return new PaymentInitResponse(
			order.getOrderNumber(),
			dto.totalAmount().longValue(),
			dto.orderItems().size() > 1
				? dto.orderItems().getFirst().itemName() + " 외 " + (dto.orderItems().size() - 1) + "건"
				: dto.orderItems().getFirst().itemName(),
			member.getMemberName()
		);
	}
}