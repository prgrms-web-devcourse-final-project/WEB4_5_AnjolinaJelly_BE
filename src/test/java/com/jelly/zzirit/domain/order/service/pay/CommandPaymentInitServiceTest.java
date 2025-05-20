package com.jelly.zzirit.domain.order.service.pay;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.member.domain.MemberFixture;
import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.domain.order.domain.fixture.OrderFixture;
import com.jelly.zzirit.domain.order.dto.request.OrderItemCreateRequest;
import com.jelly.zzirit.domain.order.dto.request.PaymentRequest;
import com.jelly.zzirit.domain.order.dto.response.PaymentInitResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.service.order.manage.CommandOrderSequence;
import com.jelly.zzirit.domain.order.service.order.manage.CommandTempOrderService;
import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;

@ExtendWith(MockitoExtension.class)
class CommandPaymentInitServiceTest {

	@InjectMocks
	private CommandPaymentInitService service;

	@Mock
	private CommandOrderSequence orderSequenceGenerator;

	@Mock
	private CommandTempOrderService tempOrderService;

	@Mock
	private MemberRepository memberRepository;

	@Test
	void 임시_주문_생성_및_응답_반환_성공() {
		// given
		Long memberId = 1L;
		Long sequence = 7L;
		String orderNumber = Order.generateOrderNumber(sequence);

		Member member = MemberFixture.일반_회원();
		Order order = OrderFixture.결제된_주문_생성(member, orderNumber);

		PaymentRequest dto = new PaymentRequest(
			List.of(
				new OrderItemCreateRequest(1L, "모나미 볼펜", 2),
				new OrderItemCreateRequest(2L, "하이테크", 1)
			),
			BigDecimal.valueOf(10000),
			"문 앞에 놔주세요",
			"서울시 강남구",
			"101호"
		);

		try (MockedStatic<AuthMember> mocked = mockStatic(AuthMember.class)) {
			mocked.when(AuthMember::getMemberId).thenReturn(memberId);
			when(orderSequenceGenerator.getTodaySequence()).thenReturn(sequence);
			when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
			when(tempOrderService.createTempOrder(dto, member, orderNumber)).thenReturn(order);

			// when
			PaymentInitResponse response = service.createOrderAndReturnInit(dto);

			// then
			assertEquals(orderNumber, response.orderId());
			assertEquals(10000, response.amount());
			assertEquals("모나미 볼펜 외 1건", response.orderName());
			assertEquals(member.getMemberName(), response.customerName());
		}
	}

	@Test
	void 회원정보가_없으면_예외를_던진다() {
		// given
		Long memberId = 1L;

		try (MockedStatic<AuthMember> mocked = mockStatic(AuthMember.class)) {
			mocked.when(AuthMember::getMemberId).thenReturn(memberId);
			when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

			// when & then
			InvalidUserException ex = assertThrows(InvalidUserException.class, () ->
				service.createOrderAndReturnInit(
					new PaymentRequest(
						List.of(new OrderItemCreateRequest(1L, "모나미 볼펜", 2)),
						BigDecimal.valueOf(15000),
						"요청사항",
						"주소",
						"상세주소"
					)
				)
			);
			assertEquals(BaseResponseStatus.USER_NOT_FOUND, ex.getStatus());
		}
	}

	@Test
	void 단일상품일때_orderName은_상품명만_사용된다() {
		// given
		Long memberId = 1L;
		Long sequence = 9L;
		String orderNumber = Order.generateOrderNumber(sequence);

		Member member = MemberFixture.일반_회원();
		Order order = OrderFixture.결제된_주문_생성(member, orderNumber);

		PaymentRequest dto = new PaymentRequest(
			List.of(new OrderItemCreateRequest(1L, "하이테크", 1)),
			BigDecimal.valueOf(10000),
			null,
			"서울시 강남구",
			"101호"
		);

		try (MockedStatic<AuthMember> mocked = mockStatic(AuthMember.class)) {
			mocked.when(AuthMember::getMemberId).thenReturn(memberId);
			when(orderSequenceGenerator.getTodaySequence()).thenReturn(sequence);
			when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
			when(tempOrderService.createTempOrder(dto, member, orderNumber)).thenReturn(order);

			// when
			PaymentInitResponse response = service.createOrderAndReturnInit(dto);

			// then
			assertEquals("하이테크", response.orderName());
		}
	}
}
