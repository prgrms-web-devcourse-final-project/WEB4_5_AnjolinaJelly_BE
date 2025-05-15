package com.jelly.zzirit.global.exception.custom;

import com.jelly.zzirit.global.dto.BaseResponseStatus;

import lombok.Getter;

@Getter
public class InvalidTimeDealException extends InvalidCustomException {
	public InvalidTimeDealException(BaseResponseStatus status) {
		super(status);
	}
}