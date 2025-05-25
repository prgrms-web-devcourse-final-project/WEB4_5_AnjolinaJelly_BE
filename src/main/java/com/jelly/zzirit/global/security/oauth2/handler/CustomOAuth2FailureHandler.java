package com.jelly.zzirit.global.security.oauth2.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidAuthenticationException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException exception) throws IOException {

		// ✅ 로그 추가
		log.error("❌ OAuth2 인증 실패 발생");
		log.error("❌ 예외 클래스: {}", exception.getClass().getName());
		log.error("❌ 예외 메시지: {}", exception.getMessage(), exception);

		Exception customException = (Exception) request.getAttribute("exception");

		BaseResponseStatus status = (customException instanceof InvalidAuthenticationException authEx)
			? authEx.getStatus()
			: BaseResponseStatus.UNAUTHORIZED;

		BaseResponseStatus responseStatus = (status != null) ? status : BaseResponseStatus.UNAUTHORIZED;

		if (!response.isCommitted()) {
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			response.setStatus(responseStatus.getHttpStatusCode());
			objectMapper.writeValue(response.getWriter(), BaseResponse.error(responseStatus, responseStatus.getMessage()));
		}
	}
}
