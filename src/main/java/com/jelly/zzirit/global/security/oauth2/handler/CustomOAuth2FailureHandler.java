package com.jelly.zzirit.global.security.oauth2.handler;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidAuthenticationException;
import com.jelly.zzirit.global.security.oauth2.handler.failurehandlers.DefaultOAuthFailureStrategy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomOAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {
	private final List<OAuthFailureHandlerStrategy> strategies;
	private final DefaultOAuthFailureStrategy defaultStrategy;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws
		IOException {
		Exception customException = (Exception) request.getAttribute("exception");

		BaseResponseStatus status = (customException instanceof InvalidAuthenticationException authEx)
			? authEx.getStatus()
			: BaseResponseStatus.UNAUTHORIZED;

		OAuthFailureHandlerStrategy strategy = strategies.stream()
			.filter(s -> s.supports(status))
			.findFirst()
			.orElse(defaultStrategy);

		strategy.handle(request, response, status);
	}
}
