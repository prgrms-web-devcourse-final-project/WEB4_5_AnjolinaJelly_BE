package com.jelly.zzirit.domain.member.service.email;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.global.exception.custom.InvalidUserException;

@ExtendWith(MockitoExtension.class)
class CommandEmailServiceTest {

	@Mock
	private EmailVerificationService emailVerificationService;

	@Mock
	private EmailAsyncProcessor emailAsyncProcessor;

	@InjectMocks
	private CommandEmailService emailService;

	@BeforeEach
	void setUp() {
		given(emailVerificationService.isAlreadyVerified(anyString())).willReturn(false);
	}

	@Test
	void 이메일_인증코드_발송_성공() {
		// given
		String email = "user@example.com";

		// when
		emailService.sendEmailVerificationCode(email);

		// then
		then(emailVerificationService).should().clearVerificationCode(email);
		then(emailAsyncProcessor).should().emailSendCode(eq(email), anyString());
		then(emailVerificationService).should().storeVerificationCode(eq(email), anyString());
	}

	@Test
	void 이메일_이미_검증된_경우_예외_발생() {
		// given
		String email = "user@example.com";
		given(emailVerificationService.isAlreadyVerified(email)).willReturn(true);

		// when, then
		assertThatThrownBy(() -> emailService.sendEmailVerificationCode(email))
			.isInstanceOf(InvalidUserException.class);
	}

	@Test
	void 인증코드_검증_성공() {
		// given
		String email = "user@example.com";
		String code = "123456";
		given(emailVerificationService.isAlreadyVerified(email)).willReturn(false);
		given(emailVerificationService.getStoredCode(email)).willReturn(code);

		// when
		emailService.verifyEmailCode(email, code);

		// then
		then(emailVerificationService).should().markAsVerified(email);
	}

	@Test
	void 인증코드_불일치_실패() {
		// given
		String email = "user@example.com";
		given(emailVerificationService.isAlreadyVerified(email)).willReturn(false);
		given(emailVerificationService.getStoredCode(email)).willReturn("654321");

		// when, then
		assertThatThrownBy(() -> emailService.verifyEmailCode(email, "123456"))
			.isInstanceOf(InvalidUserException.class);
	}
}