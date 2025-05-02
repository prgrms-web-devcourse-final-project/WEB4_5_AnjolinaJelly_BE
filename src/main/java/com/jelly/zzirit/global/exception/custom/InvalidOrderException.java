package com.jelly.zzirit.global.exception.custom;

import com.jelly.zzirit.global.dto.BaseResponseStatus;

import lombok.Getter;

@Getter
public class InvalidOrderException extends InvalidCustomException {
	public InvalidOrderException(BaseResponseStatus status) {
		super(status);
	}
}