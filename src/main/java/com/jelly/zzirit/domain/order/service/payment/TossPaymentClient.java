package com.jelly.zzirit.domain.order.service.payment;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.domain.order.dto.response.PaymentResponse;
import com.jelly.zzirit.domain.order.entity.Order;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TossPaymentClient {

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	@Value("${toss.payments.secret-key}")
	private String secretKey;

	private static final String BASE_URL = "https://api.tosspayments.com/v1/payments";

	public PaymentResponse confirmPayment(String paymentKey, String orderId, String amount) {
		String url = BASE_URL + "/confirm";
		HttpHeaders headers = createHeaders();

		String idempotencyKey = UUID.randomUUID().toString();
		headers.set("Idempotency-Key", idempotencyKey);

		Map<String, Object> body = Map.of(
				"paymentKey", paymentKey,
				"orderId", orderId,
				"amount", amount
		);

		try {
			ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
					url,
					new HttpEntity<>(body, headers),
					PaymentResponse.class
			);
			return response.getBody();
		} catch (HttpClientErrorException e) {
			throw new InvalidOrderException(BaseResponseStatus.TOSS_CONFIRM_FAILED);
		}
	}

	public PaymentResponse fetchPaymentInfo(String paymentKey) {
		String url = BASE_URL + "/" + paymentKey;
		HttpHeaders headers = createHeaders();

		try {
			ResponseEntity<String> response = restTemplate.exchange(
				url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

			return objectMapper.readValue(response.getBody(), PaymentResponse.class);
		} catch (Exception e) {
			throw new InvalidOrderException(BaseResponseStatus.TOSS_PAYMENT_VERIFY_FAILED);
		}
	}

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
			throw new InvalidOrderException(BaseResponseStatus.TOSS_REFUND_FAILED);
		}
	}

	public void validate(Order order, PaymentResponse response, String amount) {
		TossPaymentValidation.validateAll(order, response, amount);
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