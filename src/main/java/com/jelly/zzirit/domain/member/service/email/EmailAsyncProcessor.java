package com.jelly.zzirit.domain.member.service.email;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailAsyncProcessor {

	private final JavaMailSender javaMailSender;
	private final MailProperties mailProperties;

	@Value("${email.fake-sending:false}")
	private boolean fakeSending;

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
}