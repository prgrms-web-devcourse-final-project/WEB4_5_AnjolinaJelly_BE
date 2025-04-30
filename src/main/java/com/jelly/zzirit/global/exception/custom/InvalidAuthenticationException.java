package com.jelly.zzirit.global.exception.custom;

import com.jelly.zzirit.global.dto.BaseResponseStatus;

import lombok.Getter;

@Getter
public class InvalidAuthenticationException extends InvalidCustomException  {

	public InvalidAuthenticationException(BaseResponseStatus status) {
		super(status);
	}
}