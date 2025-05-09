package com.jelly.zzirit.domain.order.service.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordNotifier {

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	@Value("${discord.webhook.url}")
	private String webhookUrl;

	public void notifyRefundFailure(String orderNumber, String paymentKey, BigDecimal amount, String reason) {
		sendEmbed(
			List.of(
				field("주문번호", orderNumber),
				field("결제 키", paymentKey),
				field("금액", amount.toPlainString() + "원"),
				field("실패 사유", reason)
			)
		);
	}

	private Map<String, Object> field(String name, String value) {
		return Map.of("name", name, "value", value, "inline", false);
	}

	private void sendEmbed(List<Map<String, Object>> fields) {
		try {
			Map<String, Object> embed = Map.of(
				"title", "환불 실패",
				"fields", fields,
				"timestamp", Instant.now().toString(),
				"color", 16711680
			);

			Map<String, Object> payload = Map.of("embeds", List.of(embed));

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> entity = new HttpEntity<>(
				objectMapper.writeValueAsString(payload), headers);

			restTemplate.postForEntity(webhookUrl, entity, String.class);
		} catch (Exception e) {
			log.error("디스코드 알림 전송 실패: {}", e.getMessage(), e);
		}
	}
}