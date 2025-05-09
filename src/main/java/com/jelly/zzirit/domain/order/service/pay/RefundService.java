package com.jelly.zzirit.domain.order.service.pay;

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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.jelly.zzirit.domain.order.entity.Payment;
import com.jelly.zzirit.domain.order.repository.PaymentRepository;
import com.jelly.zzirit.domain.order.service.order.DiscordNotifier;
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
	private final DiscordNotifier discordNotifier;

	@Value("${toss.payments.secret-key}")
	private String secretKey;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void refund(String paymentKey, BigDecimal amount, String reason) {
		try {
			Payment payment = paymentRepository.findByPaymentKey(paymentKey)
				.orElseThrow(() -> new InvalidOrderException(BaseResponseStatus.PAYMENT_NOT_FOUND));

			Order order = payment.getOrder();
			requestTossRefund(paymentKey, amount, reason);

			order.changeStatus(Order.OrderStatus.FAILED);
			payment.changeStatus(Payment.PaymentStatus.FAILED);

			log.info("환불 완료: paymentKey={}, orderNumber={}, reason={}", paymentKey, order.getOrderNumber(), reason);
		} catch (Exception e) {
			notifyDiscordFailure(paymentKey, amount, e);
			throw new InvalidOrderException(BaseResponseStatus.ORDER_REFUND_FAILED);
		}
	}

	private void requestTossRefund(String paymentKey, BigDecimal amount, String reason) {
		String url = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";

		HttpHeaders headers = getHeaders();
		Map<String, Object> body = Map.of(
			"cancelReason", reason,
			"cancelAmount", amount
		);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			log.warn("Toss 환불 API 실패: status={}, body={}", response.getStatusCode(), response.getBody());
			throw new InvalidOrderException(BaseResponseStatus.TOSS_REFUND_FAILED);
		}
	}

	private void notifyDiscordFailure(String paymentKey, BigDecimal amount, Exception e) {
		String orderNumber = paymentRepository.findByPaymentKey(paymentKey)
			.map(Payment::getOrder)
			.map(Order::getOrderNumber)
			.orElse("UNKNOWN");

		discordNotifier.notifyRefundFailure(orderNumber, paymentKey, amount, e.getMessage());
	}

	private HttpHeaders getHeaders() {
		HttpHeaders headers = new HttpHeaders();
		String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Basic " + encodedKey);
		return headers;
	}
}