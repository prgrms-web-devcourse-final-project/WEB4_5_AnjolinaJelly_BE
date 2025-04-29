package com.jelly.zzirit.global.exception.custom;

import com.jelly.zzirit.global.dto.BaseResponseStatus;

import lombok.Getter;

@Getter
public class InvalidTokenException extends InvalidCustomException {

	public InvalidTokenException(BaseResponseStatus status) {
		super(status);
	}
}