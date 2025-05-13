package com.jelly.zzirit.global.exception.custom;

import com.jelly.zzirit.global.dto.BaseResponseStatus;

import lombok.Getter;

@Getter
public class InvalidItemException extends InvalidCustomException {
    public InvalidItemException(BaseResponseStatus status) {
        super(status);
    }
}