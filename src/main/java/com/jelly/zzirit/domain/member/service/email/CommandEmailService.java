package com.jelly.zzirit.domain.member.service.email;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommandEmailService {

	private final EmailVerificationService emailVerificationService;
	private final EmailAsyncProcessor emailAsyncProcessor;

	public void sendEmailVerificationCode(String email) {
		validateEmailRequest(email);
		emailVerificationService.clearVerificationCode(email);
		String verificationCode = String.valueOf(new Random().nextInt(900000) + 100000);
		log.info("인증번호 생성: {}", verificationCode);

		emailAsyncProcessor.emailSendCode(email, verificationCode);
		emailVerificationService.storeVerificationCode(email, verificationCode);
	}

	public void verifyEmailCode(String email, String inputCode) {
		validateEmailVerification(email, inputCode);
		emailVerificationService.markAsVerified(email);
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