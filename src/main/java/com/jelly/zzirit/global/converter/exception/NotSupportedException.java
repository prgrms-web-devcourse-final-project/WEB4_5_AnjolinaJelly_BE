package com.jelly.zzirit.global.converter.exception;

import com.jelly.zzirit.global.dto.BaseResponseStatus;
import com.jelly.zzirit.global.exception.custom.InvalidCustomException;

public class NotSupportedException extends InvalidCustomException {
	public NotSupportedException(BaseResponseStatus status) {
		super(status);
	}
}
