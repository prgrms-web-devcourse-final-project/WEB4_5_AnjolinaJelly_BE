package com.jelly.zzirit.global.exception.custom;

import com.jelly.zzirit.global.dto.BaseResponseStatus;

import lombok.Getter;

@Getter
public class InvalidCustomException extends RuntimeException {
	private final BaseResponseStatus status;

	public InvalidCustomException(BaseResponseStatus status) {
		super(status.getMessage());
		this.status = status;
	}

	public InvalidCustomException(BaseResponseStatus status, String detailMessage) {
		super(detailMessage);
		this.status = status;
	}
}