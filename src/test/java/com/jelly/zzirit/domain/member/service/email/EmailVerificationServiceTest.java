package com.jelly.zzirit.domain.member.service.email;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.jelly.zzirit.global.redis.RedisService;

class EmailVerificationServiceTest {

	@Mock
	private RedisService redisService;

	@InjectMocks
	private EmailVerificationService emailVerificationService;

	private static final String EMAIL = "test@example.com";

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void 이메일_요청_잠금_확인() {
		when(redisService.getData("emailAuth:" + EMAIL + ":requestLock")).thenReturn("true");

		boolean result = emailVerificationService.isRequestLocked(EMAIL);

		assertThat(result).isTrue();
	}

	@Test
	void 이메일_이미_인증됨_확인() {
		when(redisService.getData("emailAuth:" + EMAIL + ":verified")).thenReturn("true");

		boolean result = emailVerificationService.isAlreadyVerified(EMAIL);

		assertThat(result).isTrue();
	}

	@Test
	void 인증코드_저장_및_초기화() {
		emailVerificationService.storeVerificationCode(EMAIL, "123456");

		verify(redisService).setData(eq("emailAuth:" + EMAIL + ":code"), eq("123456"), eq(600L));
		verify(redisService).setData(eq("emailAuth:" + EMAIL + ":verified"), eq("false"), eq(600L));
		verify(redisService).setData(eq("emailAuth:" + EMAIL + ":requestLock"), eq("true"), eq(10L));
	}

	@Test
	void 저장된_인증코드_조회() {
		when(redisService.getData("emailAuth:" + EMAIL + ":code")).thenReturn("123456");

		String code = emailVerificationService.getStoredCode(EMAIL);

		assertThat(code).isEqualTo("123456");
	}

	@Test
	void 인증완료_처리() {
		emailVerificationService.markAsVerified(EMAIL);

		verify(redisService).setData(eq("emailAuth:" + EMAIL + ":verified"), eq("true"), eq(600L));
		verify(redisService).deleteData(eq("emailAuth:" + EMAIL + ":code"));
	}

	@Test
	void 인증코드_삭제() {
		emailVerificationService.clearVerificationCode(EMAIL);

		verify(redisService).deleteData(eq("emailAuth:" + EMAIL + ":code"));
	}
}
