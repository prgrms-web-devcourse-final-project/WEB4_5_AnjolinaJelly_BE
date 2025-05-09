package com.jelly.zzirit.domain.order.service.pay;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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
import com.jelly.zzirit.domain.order.dto.response.TossPaymentResponse;
import com.jelly.zzirit.domain.order.service.order.TempOrderService;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidOrderException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossConfirmService {

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;
	private final TempOrderService tempOrderService;

	@Value("${toss.payments.secret-key}")
	private String secretKey;

	public void confirmPayment(String paymentKey, String orderNumber, String amount) {
		confirmToToss(paymentKey, orderNumber, amount);
		TossPaymentResponse paymentInfo = fetchPaymentInfo(paymentKey);
		tempOrderService.confirmTempOrder(paymentInfo);
	}

	private void confirmToToss(String paymentKey, String orderId, String amount) {
		String url = "https://api.tosspayments.com/v1/payments/confirm";
		HttpHeaders headers = createHeaders();

		Map<String, Object> body = Map.of(
			"paymentKey", paymentKey,
			"orderId", orderId,
			"amount", amount
		);

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

		try {
			restTemplate.postForEntity(url, entity, String.class);
		} catch (HttpClientErrorException e) {
			throw new InvalidOrderException(BaseResponseStatus.TOSS_CONFIRM_FAILED);
		}
	}

	private TossPaymentResponse fetchPaymentInfo(String paymentKey) {
		String url = "https://api.tosspayments.com/v1/payments/" + paymentKey;
		HttpHeaders headers = createHeaders();
		HttpEntity<Void> entity = new HttpEntity<>(headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(
				url, HttpMethod.GET, entity, String.class);
			return objectMapper.readValue(response.getBody(), TossPaymentResponse.class);
		} catch (Exception e) {
			throw new InvalidOrderException(BaseResponseStatus.TOSS_PAYMENT_VERIFY_FAILED);
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		String encodedAuth = Base64.getEncoder()
			.encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
		headers.set("Authorization", "Basic " + encodedAuth);

		return headers;
	}
}