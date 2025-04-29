package com.jelly.zzirit.global.security.oauth2.service.token;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.jelly.zzirit.domain.member.entity.Member;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidUserException;
import com.jelly.zzirit.global.security.util.AuthConst;
import com.jelly.zzirit.global.security.util.CookieUtil;
import com.jelly.zzirit.global.security.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthTempTokenService {

	private final JwtUtil jwtUtil;
	private final TempTokenService tempTokenService;


	public Map<String, String> extractTokenData(HttpServletRequest request) {
		String tempToken = CookieUtil.getCookieValue(request, AuthConst.TOKEN_TYPE_TEMP);
		if (tempToken == null || tempToken.isEmpty()) {
			throw new InvalidUserException(BaseResponseStatus.USER_TEMP_SESSION_EXPIRED);
		}

		return tempTokenService.validateTempSignupToken(tempToken);
	}

	public void generateAndSetTokens(HttpServletResponse response, Member member) {
		String accessToken = createAccessToken(member);
		String refreshToken = createRefreshToken(member);

		response.addCookie(CookieUtil.createCookie(AuthConst.TOKEN_TYPE_ACCESS, accessToken, AuthConst.COOKIE_ACCESS_EXPIRATION));
		response.addCookie(CookieUtil.createCookie(AuthConst.TOKEN_TYPE_REFRESH, refreshToken, AuthConst.COOKIE_REFRESH_EXPIRATION));
	}

	private String createAccessToken(Member member) {
		return jwtUtil.createJwt(
			AuthConst.TOKEN_TYPE_ACCESS,
			member.getId(),
			member.getRole(),
			AuthConst.ACCESS_EXPIRATION
		);
	}

	private String createRefreshToken(Member member) {
		return jwtUtil.createJwt(
			AuthConst.TOKEN_TYPE_REFRESH,
			member.getId(),
			member.getRole(),
			AuthConst.REFRESH_EXPIRATION
		);
	}
}