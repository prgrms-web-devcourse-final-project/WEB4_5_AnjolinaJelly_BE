package com.jelly.zzirit.domain.order.service.pay;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.domain.order.dto.request.OrderItemRequestDto;
import com.jelly.zzirit.domain.order.dto.request.PaymentRequestDto;
import com.jelly.zzirit.domain.order.dto.request.RedisOrderData;
import com.jelly.zzirit.domain.order.dto.request.TossPaymentRequest;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.mapper.RedisOrderDataMapper;
import com.jelly.zzirit.domain.order.service.OrderSequenceGenerator;
import com.jelly.zzirit.domain.order.service.cache.RedisOrderCacheService;
import com.jelly.zzirit.domain.order.service.cache.stock.item.RedisStockService;
import com.jelly.zzirit.global.AuthMember;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TossPaymentService {

	private final RestTemplate restTemplate;
	private final OrderSequenceGenerator orderSequenceGenerator;
	private final RedisStockService redisStockService;
	private final RedisOrderCacheService redisOrderCacheService;
	private final RedisOrderDataMapper redisOrderDataMapper;

	@Value("${toss.payments.secret-key}")
	private String secretKey;

	public String createPayment(PaymentRequestDto dto) {
		for (OrderItemRequestDto item : dto.orderItems()) {
			redisStockService.reserveStock(
				item.getStockTargetId(),
				item.quantity(),
				item.isTimeDeal()
			);
		}

		// 주문 번호 생성
		Long todaySequence = orderSequenceGenerator.getTodaySequence();
		String orderId = Order.generateOrderNumber(todaySequence);

		// 주문 데이터 Redis 캐싱
		Member authUser = AuthMember.getAuthUser();
		RedisOrderData orderData = redisOrderDataMapper.from(dto, authUser);
		redisOrderCacheService.save(orderId, orderData, Duration.ofMinutes(30));

		// Toss 결제 요청
		TossPaymentRequest tossRequest = TossPaymentRequest.of(dto, orderId);
		return requestToToss(tossRequest);
	}

	private String requestToToss(TossPaymentRequest request) {
		String url = "https://api.tosspayments.com/v1/payments";
		HttpHeaders headers = createHeaders();
		HttpEntity<TossPaymentRequest> entity = new HttpEntity<>(request, headers);

		try {
			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				url,
				HttpMethod.POST,
				entity,
				new ParameterizedTypeReference<>() {}
			);

			Map<String, Object> responseBody = response.getBody();
			return extractRedirectUrl(responseBody);

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			throw new InvalidOrderException(BaseResponseStatus.TOSS_PAYMENT_REQUEST_FAILED);
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBasicAuth(secretKey);
		return headers;
	}

	private String extractRedirectUrl(Map<String, Object> responseBody) {
		if (responseBody == null || !responseBody.containsKey("nextRedirectUrl")) {
			throw new InvalidOrderException(BaseResponseStatus.TOSS_PAYMENT_REQUEST_FAILED);
		}
		return responseBody.get("nextRedirectUrl").toString();
	}
}