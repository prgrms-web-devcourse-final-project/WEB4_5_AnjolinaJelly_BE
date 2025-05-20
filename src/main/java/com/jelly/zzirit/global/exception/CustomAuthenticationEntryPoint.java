package com.jelly.zzirit.global.exception;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidAuthenticationException;
import com.jelly.zzirit.global.exception.custom.InvalidCustomException;
import com.jelly.zzirit.global.exception.custom.InvalidTokenException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
	private final HandlerExceptionResolver resolver;

	public CustomAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
		Exception exception = (Exception) request.getAttribute("exception");

		switch (exception) {
			case InvalidTokenException customEx ->
				resolver.resolveException(request, response, null, new InvalidCustomException(customEx.getStatus()));
			case InvalidAuthenticationException customEx ->
				resolver.resolveException(request, response, null, new InvalidCustomException(customEx.getStatus()));
			case MethodArgumentNotValidException validEx -> resolver.resolveException(request, response, null, validEx);
			case null, default -> resolver.resolveException(request, response, null,
				new InvalidCustomException(BaseResponseStatus.UNAUTHORIZED));
		}
	}
}