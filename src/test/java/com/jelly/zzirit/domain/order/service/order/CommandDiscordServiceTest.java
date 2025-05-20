package com.jelly.zzirit.domain.order.service.order;

import static org.mockito.BDDMockito.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class CommandDiscordServiceTest {

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private CommandDiscordService commandDiscordService;

	@BeforeEach
	void setWebhookUrl() throws Exception {
		Field field = CommandDiscordService.class.getDeclaredField("webhookUrl");
		field.setAccessible(true);
		field.set(commandDiscordService, "https://discord.com/api/webhooks/test-url");
	}

	@Test
	void 디스코드_환불_실패_알림_테스트() throws Exception {
		// given
		String orderNumber = "ORD20250509-TEST";
		String paymentKey = "pay_test_key";
		BigDecimal amount = new BigDecimal("12345.67");
		String reason = "테스트용 환불 실패 사유입니다.";

		given(objectMapper.writeValueAsString(any()))
			.willReturn("{\"content\": \"환불 실패\"}");

		// when
		commandDiscordService.notifyRefundFailure(orderNumber, paymentKey, amount, reason);

		// then
		verify(restTemplate).postForEntity(
			any(String.class),
			any(HttpEntity.class),
			eq(String.class)
		);
	}

	@Test
	void 디스코드_알림_JSON_직렬화_실패시_예외처리_후_postForEntity_호출되지_않음() throws Exception {
		// given
		given(objectMapper.writeValueAsString(any()))
			.willThrow(new JsonProcessingException("직렬화 실패") {});

		// when
		commandDiscordService.notifyRefundFailure("ORD-2", "pay_key", new BigDecimal("10000"), "사유");

		// then
		verify(restTemplate, never()).postForEntity(any(), any(), any());
	}

	@Test
	void 디스코드_알림_postForEntity_호출_중_예외가_발생해도_예외_전파_없이_로깅만_함() throws Exception {
		// given
		given(objectMapper.writeValueAsString(any()))
			.willReturn("{\"embeds\": [...]}");

		String expectedUrl = "https://discord.com/api/webhooks/test-url";

		doThrow(new RuntimeException("Discord 서버 에러"))
			.when(restTemplate)
			.postForEntity(
				eq(expectedUrl),
				ArgumentMatchers.<HttpEntity<String>>any(),
				eq(String.class)
			);

		// when
		commandDiscordService.notifyRefundFailure("ORD-3", "pay_key", new BigDecimal("10000"), "사유");

		// then
		verify(restTemplate)
			.postForEntity(
				eq(expectedUrl),
				ArgumentMatchers.<HttpEntity<String>>any(),
				eq(String.class)
			);
	}
}