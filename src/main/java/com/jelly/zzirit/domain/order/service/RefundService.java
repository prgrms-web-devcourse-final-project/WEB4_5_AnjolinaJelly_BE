package com.jelly.zzirit.domain.order.service;

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
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundService {

	private final RestTemplate restTemplate;

	@Value("${toss.payments.secret-key}")
	private String secretKey;

	public void refundImmediately(String orderId, BigDecimal amount) {
		try {
			String url = "https://api.tosspayments.com/v1/payments/" + orderId + "/cancel";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBasicAuth(secretKey);

			Map<String, Object> body = new HashMap<>();
			body.put("cancelReason", "결제 후 주문 처리 실패로 인한 자동 환불");
			body.put("cancelAmount", amount);

			HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

			ResponseEntity<String> response = restTemplate.exchange(
				url,
				HttpMethod.POST,
				request,
				String.class
			);

			if (!response.getStatusCode().is2xxSuccessful()) {
				throw new IllegalStateException("토스 결제 취소 API 실패: " + response.getBody());
			}
		} catch (Exception e) {
			log.error("자동 환불 실패: orderId={}, amount={}, message={}", orderId, amount, e.getMessage(), e);
			throw new IllegalStateException("환불 처리에 실패했습니다. 관리자에게 문의하세요.");
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