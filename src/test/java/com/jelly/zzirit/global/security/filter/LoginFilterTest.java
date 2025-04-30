package com.jelly.zzirit.global.security.filter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.global.security.service.TokenService;
import com.jelly.zzirit.global.support.MockServletInputStream;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class LoginFilterTest {

	private LoginFilter loginFilter;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private TokenService tokenService;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		loginFilter = new LoginFilter(authenticationManager, objectMapper, tokenService);
	}

	@Test
	void 로그인_성공() throws Exception {
		// given
		String email = "test@example.com";
		String password = "password123";

		// request mocking
		when(request.getServletPath()).thenReturn("/api/auth/basic/login");
		when(request.getInputStream()).thenReturn(new MockServletInputStream(
			objectMapper.writeValueAsBytes(Map.of("username", email, "password", password))
		));

		Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);
		when(authenticationManager.authenticate(any())).thenReturn(authentication);

		// when
		Authentication result = loginFilter.attemptAuthentication(request, response);

		// then
		verify(authenticationManager, times(1)).authenticate(any());
		assert result != null;
	}

	@Test
	void 로그인_요청_잘못된_BODY_실패() throws Exception {
		// given
		when(request.getServletPath()).thenReturn("/api/auth/basic/login");
		when(request.getInputStream()).thenThrow(new RuntimeException("파싱 실패"));

		// when & then
		assertThatThrownBy(() -> loginFilter.attemptAuthentication(request, response))
			.isInstanceOfAny(RuntimeException.class, BadCredentialsException.class);
	}
}