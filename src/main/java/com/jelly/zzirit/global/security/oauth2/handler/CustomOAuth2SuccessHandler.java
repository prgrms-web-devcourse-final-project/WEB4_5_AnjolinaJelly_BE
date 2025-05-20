package com.jelly.zzirit.global.security.oauth2.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.global.config.AppConfig;
import com.jelly.zzirit.global.security.model.MemberPrincipal;
import com.jelly.zzirit.global.security.service.TokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final TokenService tokenService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		MemberPrincipal memberPrincipal = (MemberPrincipal)authentication.getPrincipal();
		Long userId = memberPrincipal.getMemberId();
		Role role = memberPrincipal.getRole();

		log.info("OAuth 로그인 성공: 사용자 ID={}, 역할={}", userId, role);
		log.info("사용자 attributes 존재 여부: isEmpty={} / 값={}",
			memberPrincipal.getAttributes().isEmpty(),
			memberPrincipal.getAttributes());

		if (!response.isCommitted()) {
			String redirectBaseUrl = AppConfig.getRedirectBaseUrl();

			if (role == Role.ROLE_GUEST) {
				log.info("➡GUEST 사용자 → /auth/callback 으로 리다이렉트 시도");
				log.info("url = {}", redirectBaseUrl);
				response.sendRedirect(redirectBaseUrl + "/auth/callback");
				return;
			}
			tokenService.generateTokensAndSetCookies(response, userId, role);
			response.sendRedirect(redirectBaseUrl + "/");
		} else {
			log.warn("응답이 이미 커밋됨 — 리다이렉트 불가");
		}
	}
}