package com.jelly.zzirit.domain.order.service.pay;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundService {

	private final RestTemplate restTemplate;
	private final PaymentRepository paymentRepository;

	@Value("${toss.payments.secret-key}")
	private String secretKey;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void refundImmediately(String paymentKey, BigDecimal amount) {
		try {
			Payment payment = paymentRepository.findByPaymentKey(paymentKey)
				.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.PAYMENT_NOT_FOUND));

			Order order = payment.getOrder();
			requestTossRefund(paymentKey, amount);

			order.changeStatus(Order.OrderStatus.FAILED);
			payment.changeStatus(Payment.PaymentStatus.FAILED);

			log.info("자동 환불 완료: paymentKey={}, orderNumber={}", paymentKey, order.getOrderNumber());
		} catch (Exception e) {
			log.error("자동 환불 실패: paymentKey={}, amount={}, message={}", paymentKey, amount, e.getMessage(), e);
			throw new InvalidOrderException(BaseResponseStatus.ORDER_REFUND_FAILED);
		}
	}

	/**
	 * 주문 취소 요청에 따른 결제 취소
	 * @param orderId 취소할 주문의 아이디
	 * @param paymentKey 결제 정보 키
	 */
	public void refund(Long orderId, String paymentKey) {
		String url = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";

		HttpHeaders headers = getHeaders(); // 헤더 생성
		Map<String, Object> body = getBody(); // 바디 생성

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(
				url,
				HttpMethod.POST,
				request,
				String.class
			);

			if (!response.getStatusCode().is2xxSuccessful()) {
				log.error("환불 실패: status={}, body={}", response.getStatusCode(), response.getBody());
				throw new IllegalStateException("토스 결제 취소 API 실패");
			}
		} catch (Exception e) {
			log.error("환불 실패: orderId={}, message={}", orderId, e.getMessage(), e);
			throw new IllegalStateException("환불 처리 실패");
		}
	}

	private void requestTossRefund(String paymentKey, BigDecimal amount) {
		String url = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBasicAuth(secretKey);

		Map<String, Object> body = Map.of(
			"cancelReason", "결제 후 주문 처리 실패로 인한 자동 환불",
			"cancelAmount", amount
		);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw new InvalidOrderException(BaseResponseStatus.TOSS_REFUND_FAILED);
		}
	}

	private HttpHeaders getHeaders() {
		HttpHeaders headers = new HttpHeaders();
		String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Basic " + encodedKey);

		return headers;
	}

	private Map<String, Object> getBody() {
		String cancelReason = "구매자 변심으로 인한 주문 취소";
		Map<String, Object> body = new HashMap<>();

		body.put("cancelReason", cancelReason);

		return body;
	}

}