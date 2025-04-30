package com.jelly.zzirit.global.exception.custom;

import com.jelly.zzirit.global.dto.BaseResponseStatus;

public class InvalidUserException extends InvalidCustomException{
	public InvalidUserException(BaseResponseStatus status) {
		super(status);
	}
}