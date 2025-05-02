package com.jelly.zzirit.domain.order.service;

import java.math.BigDecimal;
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
}