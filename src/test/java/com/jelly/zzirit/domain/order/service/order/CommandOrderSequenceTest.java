package com.jelly.zzirit.domain.order.service.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.jelly.zzirit.domain.order.service.order.manage.CommandOrderSequence;

@ExtendWith(MockitoExtension.class)
class CommandOrderSequenceTest {

	@InjectMocks
	private CommandOrderSequence commandOrderSequence;

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
		given(valueOperations.increment(todayKey)).willReturn(expectedSeq);

		// when
		Long sequence = commandOrderSequence.getTodaySequence();

		// then
		assertEquals(expectedSeq, sequence);
		verify(valueOperations).increment(todayKey);
	}
}