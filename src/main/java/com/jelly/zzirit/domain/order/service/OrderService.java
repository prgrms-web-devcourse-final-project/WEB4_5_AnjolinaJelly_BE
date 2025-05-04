package com.jelly.zzirit.domain.order.service;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.jelly.zzirit.domain.order.dto.request.RedisOrderData;
import com.jelly.zzirit.domain.order.service.cache.RedisOrderCacheService;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumSet;
import java.util.List;

import static com.jelly.zzirit.domain.order.entity.Order.OrderStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

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
	 * CANCELLED, COMPLETED, PAID 상태인 주문 내역을 최신순으로 조회
	 * @param memberId 현재 로그인한 유저의 아이디
	 * @return 주문 리스트
	 */
	@Transactional(readOnly = true)
	public List<Order> findAllOrders(Long memberId) {
		return orderRepository.findAllByMemberIdWithItems(memberId, EnumSet.of(CANCELLED, COMPLETED, PAID));
	}

}
