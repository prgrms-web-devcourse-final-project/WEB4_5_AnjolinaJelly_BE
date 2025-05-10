package com.jelly.zzirit.domain.order.util;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;
import com.jelly.zzirit.domain.order.dto.response.TossPaymentResponse;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("toss")
@RequiredArgsConstructor
public class TossPaymentAdapter implements PaymentGateway {

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	@Value("${toss.payments.secret-key}")
	private String secretKey;

	private static final String BASE_URL = "https://api.tosspayments.com/v1/payments";

	@Override
	public void confirmPayment(String paymentKey, String orderId, String amount) {
		String url = BASE_URL + "/confirm";
		HttpHeaders headers = createHeaders();
		Map<String, Object> body = Map.of(
			"paymentKey", paymentKey,
			"orderId", orderId,
			"amount", amount
		);

		try {
			restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
		} catch (HttpClientErrorException e) {
			log.warn("토스 결제 승인 실패: {}", e.getMessage());
			throw new InvalidOrderException(BaseResponseStatus.TOSS_CONFIRM_FAILED);
		}
	}

	@Override
	public PaymentResponse fetchPaymentInfo(String paymentKey) {
		String url = BASE_URL + "/" + paymentKey;
		HttpHeaders headers = createHeaders();

		try {
			ResponseEntity<String> response = restTemplate.exchange(
				url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

			TossPaymentResponse tossResponse = objectMapper.readValue(response.getBody(), TossPaymentResponse.class);

			return new PaymentResponse(
				tossResponse.getOrderId(),
				tossResponse.getPaymentKey(),
				tossResponse.getMethod(),
				tossResponse.getStatus(),
				tossResponse.getTotalAmount()
			);
		} catch (Exception e) {
			log.warn("토스 결제 조회 실패: {}", e.getMessage());
			throw new InvalidOrderException(BaseResponseStatus.TOSS_PAYMENT_VERIFY_FAILED);
		}
	}

	@Override
	public void refund(String paymentKey, BigDecimal amount, String reason) {
		String url = BASE_URL + "/" + paymentKey + "/cancel";
		HttpHeaders headers = createHeaders();
		Map<String, Object> body = Map.of(
			"cancelReason", reason,
			"cancelAmount", amount
		);

		ResponseEntity<String> response = restTemplate.exchange(
			url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			log.warn("토스 환불 실패: {}, {}", response.getStatusCode(), response.getBody());
			throw new InvalidOrderException(BaseResponseStatus.TOSS_REFUND_FAILED);
		}
	}

	private HttpHeaders createHeaders() {
		String auth = Base64.getEncoder()
			.encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Basic " + auth);
		return headers;
	}
}