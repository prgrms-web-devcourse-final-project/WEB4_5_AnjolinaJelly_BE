package com.jelly.zzirit.global.exception.custom;

public class TimeDealQueueException extends RuntimeException {

    public TimeDealQueueException(String message) {
        super(message);
    }

    public TimeDealQueueException(String message, Throwable cause) {
        super(message, cause);
    }

}
