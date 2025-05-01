package com.jelly.zzirit.global.config.securityconfig;

import java.util.Collections;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.jelly.zzirit.global.config.AppConfig;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class CorsConfig implements CorsConfigurationSource {
	private final List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

	@Override
	public CorsConfiguration getCorsConfiguration(@NonNull HttpServletRequest request) {
		CorsConfiguration config = new CorsConfiguration();
//		config.setAllowedOriginPatterns(Collections.singletonList(AppConfig.getSiteFrontUrl()));
		config.setAllowedOriginPatterns(AppConfig.getAllowedOrigins()); // 여러 origin 허용
		config.setAllowedMethods(allowedMethods);
		config.setAllowCredentials(true);
		config.setAllowedHeaders(Collections.singletonList("*"));
		config.setMaxAge(3600L);
		return config;
	}
}