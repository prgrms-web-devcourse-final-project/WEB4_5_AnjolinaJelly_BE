package com.jelly.zzirit.domain.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.domain.member.dto.request.EmailAuthRequest;
import com.jelly.zzirit.domain.member.dto.request.EmailAuthVerifyRequest;
import com.jelly.zzirit.domain.member.dto.request.SignupRequest;
import com.jelly.zzirit.global.redis.RedisService;
import com.jelly.zzirit.global.redis.TestContainerConfig;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerRabbitTest extends TestContainerConfig {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	RedisService redisService;

	@Test
	void 이메일_인증코드_발송_성공() throws Exception {
		redisService.deleteData("emailAuth:test@example.com:requestLock");
		redisService.deleteData("emailAuth:test@example.com:verified");

		EmailAuthRequest dto = new EmailAuthRequest("test@example.com");

		mockMvc.perform(post("/api/auth/send-email-code")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(dto)))
			.andExpect(status().isOk());
	}

	@Test
	void 이메일_인증코드_검증_성공() throws Exception {
		redisService.setData("emailAuth:test@example.com:code", "123456", 600L);
		redisService.setData("emailAuth:test@example.com:verified", "false", 600L);

		EmailAuthVerifyRequest dto = new EmailAuthVerifyRequest("test@example.com", "123456");

		mockMvc.perform(post("/api/auth/verify-email")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(dto)))
			.andExpect(status().isOk());
	}

	@Test
	void 회원가입_성공() throws Exception {
		redisService.setData("emailAuth:test@example.com:verified", "true", 600L);

		SignupRequest dto = new SignupRequest("홍길동", "test@example.com", "password123", "서울시 강남구", "302호");

		mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(dto)))
			.andExpect(status().isOk());
	}

	private static String asJsonString(Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}