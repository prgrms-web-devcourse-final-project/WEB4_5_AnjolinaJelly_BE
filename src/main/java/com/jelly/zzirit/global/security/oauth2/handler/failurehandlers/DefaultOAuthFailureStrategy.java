package com.jelly.zzirit.global.security.oauth2.handler.failurehandlers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.global.config.AppConfig;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.security.oauth2.handler.OAuthFailureHandlerStrategy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DefaultOAuthFailureStrategy implements OAuthFailureHandlerStrategy {

	private final ObjectMapper objectMapper;

	@Override
	public boolean supports(BaseResponseStatus status) {
		return true;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, BaseResponseStatus status) throws
		IOException {
		BaseResponseStatus responseStatus = (status != null) ? status : BaseResponseStatus.UNAUTHORIZED;

		if (!response.isCommitted()) {
			response.setStatus(responseStatus.getHttpStatus());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			objectMapper.writeValue(response.getWriter(), BaseResponse.error(responseStatus, responseStatus.getMessage()));
		}

		String redirectUrl = UriComponentsBuilder.fromUriString(AppConfig.getSiteFrontUrl() + "/login")
			.queryParam("error", responseStatus.getMessage())
			.build()
			.toUriString();

		response.sendRedirect(redirectUrl);
	}
}