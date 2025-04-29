package com.jelly.zzirit.global.security.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidAuthenticationException;
import com.jelly.zzirit.global.security.model.MemberPrincipal;
import com.jelly.zzirit.global.security.util.AuthConst;
import com.jelly.zzirit.global.security.util.CookieUtil;
import com.jelly.zzirit.global.security.util.JwtUtil;
import com.jelly.zzirit.global.security.util.RedisRefreshTokenUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

	private final JwtUtil jwtUtil;
	private final RedisRefreshTokenUtil redisRefreshTokenUtil;

	public boolean isAccessToken(String token) {
		return "access".equals(jwtUtil.getCategory(token));
	}

	public Authentication getAuthenticationFromToken(String token) {
		Long userId = jwtUtil.getUserId(token);
		Role role = jwtUtil.getRole(token);
		MemberPrincipal memberPrincipal = new MemberPrincipal(userId, role);
		return new UsernamePasswordAuthenticationToken(memberPrincipal, null, memberPrincipal.getAuthorities());
	}

	public String generateTokensAndSetCookies(HttpServletResponse response, Long userId, Role role) {
		String newAccessToken = jwtUtil.createJwt(AuthConst.TOKEN_TYPE_ACCESS, userId, role, AuthConst.ACCESS_EXPIRATION);
		String newRefreshToken = jwtUtil.createJwt(AuthConst.TOKEN_TYPE_REFRESH, userId, role, AuthConst.REFRESH_EXPIRATION);

		redisRefreshTokenUtil.deleteRefreshToken(userId);
		redisRefreshTokenUtil.addRefreshToken(userId, newRefreshToken, AuthConst.REFRESH_EXPIRATION);

		response.addCookie(CookieUtil.createCookie(AuthConst.TOKEN_TYPE_ACCESS, newAccessToken, AuthConst.COOKIE_ACCESS_EXPIRATION));
		response.addCookie(CookieUtil.createCookie(AuthConst.TOKEN_TYPE_REFRESH, newRefreshToken, AuthConst.COOKIE_REFRESH_EXPIRATION));

		SecurityContextHolder.getContext().setAuthentication(getAuthenticationFromToken(newAccessToken));
		return newAccessToken;
	}

	public String rotatingTokens(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = CookieUtil.getCookieValue(request, AuthConst.TOKEN_TYPE_REFRESH);

		if (refreshToken == null || refreshToken.isEmpty() || jwtUtil.isExpired(refreshToken)) {
			throw new InvalidAuthenticationException(BaseResponseStatus.REFRESH_TOKEN_EXPIRED);
		}

		RefreshTokenValidationStatus.validateToken(refreshToken, jwtUtil, redisRefreshTokenUtil);
		Long userId = jwtUtil.getUserId(refreshToken);
		Role role = jwtUtil.getRole(refreshToken);
		return generateTokensAndSetCookies(response, userId, role);
	}
}