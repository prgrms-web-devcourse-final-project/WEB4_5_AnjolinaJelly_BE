package com.jelly.zzirit.domain.order.service.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class OrderSequenceGeneratorTest {

	@InjectMocks
	private OrderSequenceGenerator orderSequenceGenerator;

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Test
	void 정상적으로_시퀀스를_반환() {
		// given
		String todayKey = "order:seq:" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
		Long expectedSeq = 42L;

		given(redisTemplate.opsForValue()).willReturn(valueOperations);
		given(valueOperations.setIfAbsent(eq(todayKey), eq("0"), any(Duration.class))).willReturn(true);
		given(valueOperations.increment(todayKey)).willReturn(expectedSeq);

		// when
		Long sequence = orderSequenceGenerator.getTodaySequence();

		// then
		assertEquals(expectedSeq, sequence);
		verify(valueOperations).setIfAbsent(eq(todayKey), eq("0"), any(Duration.class));
		verify(valueOperations).increment(todayKey);
	}
}