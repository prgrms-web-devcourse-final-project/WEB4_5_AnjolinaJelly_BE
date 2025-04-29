package com.jelly.zzirit.global.exception;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidCustomException;
import com.jelly.zzirit.global.exception.custom.InvalidTokenException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
	private final HandlerExceptionResolver resolver;

	public CustomAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
		Exception exception = (Exception) request.getAttribute("exception");

		if (exception instanceof InvalidTokenException customEx) {
			resolver.resolveException(request, response, null, new InvalidCustomException(customEx.getStatus()));
		} else {
			resolver.resolveException(request, response, null, new InvalidCustomException(BaseResponseStatus.UNAUTHORIZED));
		}
	}
}