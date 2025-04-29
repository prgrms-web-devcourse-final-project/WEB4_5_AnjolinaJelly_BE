package com.jelly.zzirit.global.security.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.global.security.util.AuthConst;
import com.jelly.zzirit.global.security.util.CookieUtil;
import com.jelly.zzirit.global.security.util.JwtUtil;
import com.jelly.zzirit.global.security.util.RedisBlacklistUtil;
import com.jelly.zzirit.global.security.util.RedisRefreshTokenUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

	private final JwtUtil jwtUtil;
	private final RedisBlacklistUtil redisBlacklistUtil;
	private final RedisRefreshTokenUtil redisRefreshTokenUtil;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

		String accessToken = CookieUtil.getCookieValue(request, AuthConst.TOKEN_TYPE_ACCESS);
		if (accessToken != null && !jwtUtil.isExpired(accessToken)) {
			long ttl = jwtUtil.getExpiration(accessToken);
			redisBlacklistUtil.addToBlacklist(accessToken, ttl);
		}

		String refreshToken = CookieUtil.getCookieValue(request, AuthConst.TOKEN_TYPE_REFRESH);
		if (refreshToken != null && !jwtUtil.isExpired(refreshToken)) {
			Long userId = jwtUtil.getUserId(refreshToken);
			redisRefreshTokenUtil.deleteRefreshToken(userId);
		}

		response.addCookie(CookieUtil.deleteCookie(AuthConst.TOKEN_TYPE_ACCESS));
		response.addCookie(CookieUtil.deleteCookie(AuthConst.TOKEN_TYPE_REFRESH));
	}
}
