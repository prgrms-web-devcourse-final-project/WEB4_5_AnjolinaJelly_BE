package com.jelly.zzirit.global.exception.custom;

import com.jelly.zzirit.global.dto.BaseResponseStatus;

public class LockAcquisitionFailureException extends InvalidCustomException {

    public LockAcquisitionFailureException(BaseResponseStatus status) {
        super(status);
    }

}
