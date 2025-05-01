package com.jelly.zzirit.domain.member.controller;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.domain.member.dto.request.EmailAuthDTO;
import com.jelly.zzirit.domain.member.dto.request.EmailAuthVerificationDTO;
import com.jelly.zzirit.domain.member.dto.request.SignupDTO;
import com.jelly.zzirit.global.redis.RedisService;
import com.jelly.zzirit.global.support.OpenApiDocumentationFilter;
import com.jelly.zzirit.global.support.RestDocsSupport;

@ActiveProfiles("test")
class AuthControllerTest extends RestDocsSupport {

	@Autowired
	RedisService redisService;

	@Test
	void 이메일_인증코드_발송_API_문서() {
		EmailAuthDTO request = new EmailAuthDTO("test@naver.com");

		this.spec
			.filter(OpenApiDocumentationFilter.of(
				"auth-send-email-code",
				new FieldDescriptor[]{
					fieldWithPath("email").description("사용자 이메일")
				}
			))
			.body(asJsonString(request))
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.when()
			.post("/api/auth/send-email-code")
			.then()
			.statusCode(200);
	}

	@Test
	void 이메일_인증코드_검증_API_문서() {
		redisService.setData("emailAuth:test@example.com:code", "123456", 600L);
		redisService.setData("emailAuth:test@example.com:verified", "false", 600L);

		EmailAuthVerificationDTO request = new EmailAuthVerificationDTO("test@example.com", "123456");

		this.spec
			.filter(OpenApiDocumentationFilter.of(
				"auth-verify-email",
				new FieldDescriptor[]{
					fieldWithPath("email").description("사용자 이메일"),
					fieldWithPath("code").description("인증 코드")
				}
			))
			.body(asJsonString(request))
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.when()
			.post("/api/auth/verify-email")
			.then()
			.statusCode(200);
	}

	@Test
	void 회원가입_API_문서() {
		redisService.setData("emailAuth:test@example.com:verified", "true", 600L);

		SignupDTO request = new SignupDTO("홍길동", "test@example.com", "password123", "서울시 강남구", "302호");

		this.spec
			.filter(OpenApiDocumentationFilter.of(
				"auth-signup",
				new FieldDescriptor[]{
					fieldWithPath("memberName").description("회원 이름"),
					fieldWithPath("memberEmail").description("회원 이메일"),
					fieldWithPath("memberPassword").description("회원 비밀번호"),
					fieldWithPath("memberAddress").description("회원 기본 주소").optional(),
					fieldWithPath("memberAddressDetail").description("회원 상세 주소").optional()
				}
			))
			.body(asJsonString(request))
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.when()
			.post("/api/auth/signup")
			.then()
			.statusCode(200);
	}

	private static String asJsonString(Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}