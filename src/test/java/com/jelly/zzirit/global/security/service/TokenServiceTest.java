package com.jelly.zzirit.global.security.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidAuthenticationException;
import com.jelly.zzirit.global.security.util.AuthConst;
import com.jelly.zzirit.global.security.util.CookieUtil;
import com.jelly.zzirit.global.security.util.JwtUtil;
import com.jelly.zzirit.global.security.util.RedisRefreshTokenUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

	@InjectMocks
	private TokenService tokenService;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private RedisRefreshTokenUtil redisRefreshTokenUtil;

	@Mock
	private HttpServletResponse response;

	@Mock
	private HttpServletRequest request;

	private final Long userId = 1L;
	private final Role role = Role.ROLE_USER;

	@Test
	void 토큰_발급_및_쿠키_설정() {
		// given
		String accessToken = "newAccessToken";
		String refreshToken = "newRefreshToken";

		given(jwtUtil.createJwt(eq(AuthConst.TOKEN_TYPE_ACCESS), any(), any(), anyLong()))
			.willReturn(accessToken);
		given(jwtUtil.createJwt(eq(AuthConst.TOKEN_TYPE_REFRESH), any(), any(), anyLong()))
			.willReturn(refreshToken);

		// when
		String issuedAccessToken = tokenService.generateTokensAndSetCookies(response, userId, role);

		// then
		assertThat(issuedAccessToken).isEqualTo(accessToken);
		verify(redisRefreshTokenUtil).deleteRefreshToken(userId);
		verify(redisRefreshTokenUtil).addRefreshToken(eq(userId), eq(refreshToken), eq(AuthConst.REFRESH_EXPIRATION));
		verify(response, times(2)).addCookie(any(Cookie.class));
	}

	@Test
	void 리프레시_토큰_없으면_예외발생() {
		// given
		given(CookieUtil.getCookieValue(request, AuthConst.TOKEN_TYPE_REFRESH)).willReturn(null);

		// when & then
		assertThatThrownBy(() -> tokenService.rotatingTokens(request, response))
			.isInstanceOf(InvalidAuthenticationException.class)
			.hasMessageContaining(BaseResponseStatus.REFRESH_TOKEN_EXPIRED.getMessage());
	}
}