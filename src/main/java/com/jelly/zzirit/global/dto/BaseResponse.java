package com.jelly.zzirit.global.dto;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"success", "code", "httpStatus", "message", "result"})
public class BaseResponse<T> {

	private final boolean success;

	private final int code;

	private final HttpStatus httpStatus;

	private final String message;

	private final T result;

	// 요청에 성공한 경우 - 결과 값이 없을 때
	public static BaseResponse<Empty> success() {
		return new BaseResponse<>(BaseResponseStatus.SUCCESS, Empty.getInstance());
	}

	// 요청에 성공한 경우 - 결과 값이 있을 때
	public static <T> BaseResponse<T> success(T result) {
		return new BaseResponse<>(BaseResponseStatus.SUCCESS, result);
	}

	// 요청에 실패한 경우 - 결과 값이 없을 때
	public static BaseResponse<Empty> error(BaseResponseStatus status) {
		return new BaseResponse<>(status, Empty.getInstance());
	}

	// 요청에 실패한 경우 - 추가 메시지 또는 데이터가 있는 경우
	public static <T> BaseResponse<T> error(BaseResponseStatus status, T result) {
		return new BaseResponse<>(status, result);
	}

	private BaseResponse(BaseResponseStatus status, T result) {
		this.success = status.isSuccess();
		this.code = status.getCode();
		this.httpStatus = status.getHttpStatus();
		this.message = status.getMessage();
		this.result = result;
	}
}
