package com.jelly.zzirit.domain.order.service.order;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.jelly.zzirit.global.redis.TestRedisTemplateConfig;
import com.jelly.zzirit.global.redis.TestRedissonConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestRedissonConfig.class, TestRedisTemplateConfig.class})
class DiscordNotifierTest {

	@Autowired
	private DiscordNotifier discordNotifier;

	@Test
	void 디스코드_환불_실패_알림_테스트() {
		String orderNumber = "ORD20250509-TEST";
		String paymentKey = "pay_test_key";
		BigDecimal amount = new BigDecimal("12345.67");
		String reason = "테스트용 환불 실패 사유입니다.";

		discordNotifier.notifyRefundFailure(orderNumber, paymentKey, amount, reason);
	}
}