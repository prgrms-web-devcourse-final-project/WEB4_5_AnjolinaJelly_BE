package com.jelly.zzirit.global.security.oauth2.handler.failurehandlers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.security.oauth2.handler.OAuthFailureHandlerStrategy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuthUserNotFoundStrategy implements OAuthFailureHandlerStrategy {
	private final ObjectMapper objectMapper;

	@Override
	public boolean supports(BaseResponseStatus status) {
		return status == BaseResponseStatus.OAUTH_USER_NOT_FOUND;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, BaseResponseStatus status) throws
		IOException {
		response.setStatus(status.getHttpStatus());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		objectMapper.writeValue(response.getWriter(), BaseResponse.error(status));
	}
}