package com.jelly.zzirit.domain.member.service.auth;

import com.jelly.zzirit.domain.member.dto.request.SignupRequest;
import com.jelly.zzirit.domain.member.repository.MemberRepository;
import com.jelly.zzirit.domain.member.util.PasswordManager;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;
import com.jelly.zzirit.global.redis.RedisService;

enum SignupValidationStatus {

	EMAIL_NOT_VERIFIED {
		@Override
		public void validate(SignupRequest signupRequest, MemberRepository memberRepository, RedisService redisService, PasswordManager passwordManager) {
			String emailVerified = redisService.getData("emailAuth:" + signupRequest.getMemberEmail() + ":verified");
			if (!"true".equals(emailVerified)) {
				throw new InvalidUserException(BaseResponseStatus.EMAIL_VERIFICATION_REQUIRED);
			}
		}
	},

	PASSWORD_INVALID {
		@Override
		public void validate(SignupRequest signupRequest, MemberRepository memberRepository, RedisService redisService, PasswordManager passwordManager) {
			if (passwordManager.isInvalid(signupRequest.getMemberPassword())) {
				throw new InvalidUserException(BaseResponseStatus.USER_PASSWORD_INVALID);
			}
		}
	},

	USER_ALREADY_EXISTS {
		@Override
		public void validate(SignupRequest signupRequest, MemberRepository memberRepository, RedisService redisService, PasswordManager passwordManager) {
			if (memberRepository.findByMemberEmail(signupRequest.getMemberEmail()).isPresent()) {
				throw new InvalidUserException(BaseResponseStatus.USER_ALREADY_EXISTS);
			}
		}
	};

	abstract void validate(SignupRequest signupRequest, MemberRepository memberRepository, RedisService redisService, PasswordManager passwordManager);

	public static void validateAll(SignupRequest signupRequest, MemberRepository memberRepository, RedisService redisService, PasswordManager passwordManager) {
		for (SignupValidationStatus status : SignupValidationStatus.values()) {
			status.validate(signupRequest, memberRepository, redisService, passwordManager);
		}
	}
}