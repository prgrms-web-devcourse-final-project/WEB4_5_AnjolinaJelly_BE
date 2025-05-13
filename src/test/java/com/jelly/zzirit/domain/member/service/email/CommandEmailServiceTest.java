package com.jelly.zzirit.domain.member.service.email;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.jelly.zzirit.global.exception.custom.InvalidUserException;

class CommandEmailServiceTest {

	@Mock
	private JavaMailSender javaMailSender;

	@Mock
	private EmailVerificationService emailVerificationService;

	@Mock
	private MailProperties mailProperties;

	@InjectMocks
	private CommandEmailService commandEmailService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		given(mailProperties.getUsername()).willReturn("test@example.com");
	}

	@Test
	void 이메일_인증코드_발송_성공() {
		// given
		String email = "user@example.com";
		given(emailVerificationService.isRequestLocked(email)).willReturn(false);
		given(emailVerificationService.isAlreadyVerified(email)).willReturn(false);

		// when
		commandEmailService.sendEmailVerificationCode(email);

		// then
		then(javaMailSender).should().send(any(SimpleMailMessage.class));
		then(emailVerificationService).should().clearVerificationCode(email);
		then(emailVerificationService).should().storeVerificationCode(eq(email), anyString());
	}

	@Test
	void 이메일_이미_검증된_경우_예외_발생() {
		// given
		String email = "user@example.com";
		given(emailVerificationService.isRequestLocked(email)).willReturn(false);
		given(emailVerificationService.isAlreadyVerified(email)).willReturn(true);

		// when, then
		assertThatThrownBy(() -> commandEmailService.sendEmailVerificationCode(email))
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
		commandEmailService.verifyEmailCode(email, code);

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
		assertThatThrownBy(() -> commandEmailService.verifyEmailCode(email, "123456"))
			.isInstanceOf(InvalidUserException.class);
	}
}
