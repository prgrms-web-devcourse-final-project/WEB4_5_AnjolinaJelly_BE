package com.jelly.zzirit.global.converter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.global.converter.exception.NotSupportedException;
import com.jelly.zzirit.global.dto.BaseResponse;
import com.jelly.zzirit.global.dto.BaseResponseStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BaseHttpsMessageConverter extends AbstractHttpMessageConverter<BaseResponse<Object>> {

	private final ObjectMapper objectMapper;

	@Override
	public List<MediaType> getSupportedMediaTypes() {
		return Collections.singletonList(MediaType.APPLICATION_JSON);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return clazz.equals(String.class);
	}

	@Override
	protected BaseResponse<Object> readInternal(Class<? extends BaseResponse<Object>> clazz,
		HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		throw new NotSupportedException(BaseResponseStatus.NOT_SUPPORTED);
	}

	@Override
	protected void writeInternal(BaseResponse<Object> resultMessage, HttpOutputMessage outputMessage) throws
		IOException,
		HttpMessageNotWritableException {
		String responseMessage = this.objectMapper.writeValueAsString(resultMessage);
		StreamUtils.copy(responseMessage.getBytes(StandardCharsets.UTF_8), outputMessage.getBody());
	}
}
