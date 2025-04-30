package com.jelly.zzirit.domain.member.service.email;

import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender javaMailSender;
	private final EmailVerificationService emailVerificationService;
	private final MailProperties mailProperties;

	@Value("${email.fake-sending:false}")
	private boolean fakeSending;

	public void sendEmailVerificationCode(String email) {
		validateEmailRequest(email);
		emailVerificationService.clearVerificationCode(email);
		String verificationCode = String.valueOf(new Random().nextInt(900000) + 100000);
		log.info("인증번호 생성: {}", verificationCode);

		emailSendCode(email, verificationCode);
		emailVerificationService.storeVerificationCode(email, verificationCode);
	}

	public void verifyEmailCode(String email, String inputCode) {
		validateEmailVerification(email, inputCode);
		emailVerificationService.markAsVerified(email);
	}

	@Async("mailExecutor")
	@Retryable(noRetryFor = {IllegalArgumentException.class}, backoff = @Backoff(delay = 1000, multiplier = 2))
	public void emailSendCode(String toEmail, String verificationCode) {
		if (fakeSending) {
			log.info("[FAKE EMAIL] 인증 코드 {}가 {}로 전송된 것으로 간주합니다.", verificationCode, toEmail);
			return;
		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(toEmail);
		message.setFrom(mailProperties.getUsername());
		message.setSubject("찌릿 이메일 인증 코드");
		message.setText("인증 코드는 " + verificationCode + " 입니다.");
		javaMailSender.send(message);
	}

	@Recover
	@SuppressWarnings("unused")
	public void recover(MailException e, String toEmail, String verificationCode) {
		log.error("메일 전송 재시도 실패, 대상 이메일: {}, 인증 코드: {}, 예외: {}", toEmail, verificationCode, e.getMessage());
	}

	private void validateEmailRequest(String email) {
		if (emailVerificationService.isRequestLocked(email)) {
			throw new InvalidUserException(BaseResponseStatus.EMAIL_REQUEST_LOCKED);
		}

		if (emailVerificationService.isAlreadyVerified(email)) {
			throw new InvalidUserException(BaseResponseStatus.EMAIL_ALREADY_VERIFIED);
		}
	} // 이메일 전송 가드

	private void validateEmailVerification(String email, String inputCode) {
		if (emailVerificationService.isAlreadyVerified(email)) {
			throw new InvalidUserException(BaseResponseStatus.EMAIL_ALREADY_VERIFIED);
		}

		String storedCode = emailVerificationService.getStoredCode(email);
		if (storedCode == null || !storedCode.equals(inputCode)) {
			throw new InvalidUserException(BaseResponseStatus.EMAIL_INVALID_CODE);
		}
	} // 이메일 인증코드 가드
}