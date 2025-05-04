package com.jelly.zzirit.domain.order.service;

import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.domain.order.dto.request.RedisOrderData;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import com.jelly.zzirit.domain.order.service.cache.RedisOrderCacheService;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static com.jelly.zzirit.global.dto.BaseResponseStatus.ACCESS_DENIED;
import static com.jelly.zzirit.global.dto.BaseResponseStatus.ORDER_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandOrderService {

	private final RedisOrderCacheService redisOrderCacheService;
	private final PostPaymentProcessor postPaymentProcessor;
	private final RefundService refundService;
	private final OrderRepository orderRepository;

	@Transactional(timeout = 5, isolation = Isolation.READ_COMMITTED)
	public void confirmPayment(String orderId) {
		RedisOrderData cached = redisOrderCacheService.get(orderId)
			.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.ORDER_CACHE_NOT_FOUND));

		try {
			postPaymentProcessor.process(orderId, cached);
		} catch (Exception e) {
			refundService.refundImmediately(orderId, cached.getTotalAmount());
			log.error("주문 처리 실패로 환불 시도됨. orderId={}, reason={}", orderId, e.getMessage(), e);
			throw new InvalidOrderException(BaseResponseStatus.ORDER_PROCESSING_FAILED_AFTER_PAYMENT);
		}
	}

	/**
	 * 주문 취소
	 * @param orderId 취소할 주문의 아이디
	 * @param memberId 현재 로그인한 유저의 아이디
	 * @param role 현재 로그인한 유저의 역할
	 */
	@Transactional
	public void cancelOrder(Long orderId, Long memberId, Role role) {
		Order order = orderRepository.findByIdWithMember(orderId)
			.orElseThrow(() -> new InvalidOrderException(ORDER_NOT_FOUND));

		boolean isAdmin = (role == Role.ROLE_ADMIN);

		// 관리자나 주문을 접수한 유저만 주문 취소 가능
		if (!isAdmin && !order.isOwnedBy(memberId)) {
			throw new InvalidOrderException(ACCESS_DENIED);
		}

		order.cancel();
	}

}
