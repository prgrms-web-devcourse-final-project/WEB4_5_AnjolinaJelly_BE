package com.jelly.zzirit.global.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	@Bean
	public RestTemplate restTemplate() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(1000); // 1초 (TCP 연결 타임아웃)
		factory.setReadTimeout(8000);    // 8초 (응답 대기 타임아웃)

		RestTemplate restTemplate = new RestTemplate(factory);

		// 문자 인코딩 등 메시지 컨버터 설정 (기존 코드 유지)
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		messageConverters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
		messageConverters.addAll(restTemplate.getMessageConverters());
		restTemplate.setMessageConverters(messageConverters);

		return restTemplate;
	}
}