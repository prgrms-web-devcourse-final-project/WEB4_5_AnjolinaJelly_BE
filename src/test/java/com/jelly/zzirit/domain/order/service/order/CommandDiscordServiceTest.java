package com.jelly.zzirit.domain.order.service.order;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@ActiveProfiles("test")
class CommandDiscordServiceTest {

	@MockitoBean
	private RestTemplate restTemplate;

	@Autowired
	private CommandDiscordService commandDiscordService;

	@Test
	void 디스코드_환불_실패_알림_테스트() {
		String orderNumber = "ORD20250509-TEST";
		String paymentKey = "pay_test_key";
		BigDecimal amount = new BigDecimal("12345.67");
		String reason = "테스트용 환불 실패 사유입니다.";

		commandDiscordService.notifyRefundFailure(orderNumber, paymentKey, amount, reason);
		verify(restTemplate).postForEntity(any(), any(), eq(String.class));
	}
}