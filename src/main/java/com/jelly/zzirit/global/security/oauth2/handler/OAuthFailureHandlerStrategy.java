package com.jelly.zzirit.global.security.oauth2.handler;

import java.io.IOException;

import com.jelly.zzirit.global.dto.BaseResponseStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface OAuthFailureHandlerStrategy {
	boolean supports(BaseResponseStatus status);
	void handle(HttpServletRequest request, HttpServletResponse response, BaseResponseStatus status) throws IOException;
}