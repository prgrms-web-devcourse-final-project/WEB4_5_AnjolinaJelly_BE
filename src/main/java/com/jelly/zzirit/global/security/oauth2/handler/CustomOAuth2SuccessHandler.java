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

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final TokenService tokenService;

	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
		MemberPrincipal memberPrincipal = (MemberPrincipal) authentication.getPrincipal();
		Long userId = memberPrincipal.getMemberId();
		Role role = memberPrincipal.getRole();

		if (!response.isCommitted()) {
			if (role == Role.ROLE_GUEST) {
				response.sendRedirect(AppConfig.getSiteFrontUrl() + "/social-signup");
				return;
			} // 추가 정보를 받는 경우
			tokenService.generateTokensAndSetCookies(response, userId, role);
			response.sendRedirect(AppConfig.getSiteFrontUrl()  + "/");
		}
	}
}