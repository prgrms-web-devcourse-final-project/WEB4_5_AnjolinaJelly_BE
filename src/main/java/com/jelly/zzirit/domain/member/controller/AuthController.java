package com.jelly.zzirit.domain.member.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jelly.zzirit.domain.member.dto.request.EmailAuthDTO;
import com.jelly.zzirit.domain.member.dto.request.EmailAuthVerificationDTO;
import com.jelly.zzirit.domain.member.dto.request.SignupDTO;
import com.jelly.zzirit.domain.member.dto.request.SocialSignupDTO;
import com.jelly.zzirit.domain.member.service.auth.AuthService;
import com.jelly.zzirit.domain.member.service.email.EmailService;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.Empty;
import com.jelly.zzirit.global.security.oauth2.service.signup.FirstOAuthSignUpService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "자체 회원가입 및 소셜 회원가입 관련 API")
public class AuthController {

	private final AuthService authService;
	private final FirstOAuthSignUpService firstOAuthSignUpService;
	private final EmailService emailService;

	@Operation(summary = "이메일 인증 코드 전송")
	@PostMapping("/send-email-code")
	public BaseResponse<Empty> sendEmailVerificationCode(@RequestBody @Valid EmailAuthDTO emailAuthDto) {
		emailService.sendEmailVerificationCode(emailAuthDto.getEmail());
		return BaseResponse.success();
	}

	@Operation(summary = "이메일 인증 코드 검증")
	@PostMapping("/verify-email")
	public BaseResponse<Empty> verifyEmailCode(@RequestBody @Valid EmailAuthVerificationDTO emailAuthVerificationDto) {
		emailService.verifyEmailCode(emailAuthVerificationDto.getEmail(), emailAuthVerificationDto.getCode());
		return BaseResponse.success();
	}

	@Operation(summary = "자체 회원가입")
	@PostMapping("/signup")
	public BaseResponse<Empty> signup(@RequestBody @Valid SignupDTO signupDTO) {
		authService.signup(signupDTO);
		return BaseResponse.success();
	}

	@Operation(summary = "소셜 회원가입 최종 처리")
	@PostMapping("/social-signup")
	public BaseResponse<Empty> completeSignup(HttpServletRequest request, HttpServletResponse response,
		@RequestBody @Valid SocialSignupDTO socialSignupDto) {
		firstOAuthSignUpService.finalizeSocialSignup(request, response, socialSignupDto);
		return BaseResponse.success();
	}
}