package com.jelly.zzirit.global.exception.custom;

import com.jelly.zzirit.global.dto.BaseResponseStatus;

public class InvalidRedisException extends InvalidCustomException {
	public InvalidRedisException(BaseResponseStatus status) {
		super(status);
	}
}