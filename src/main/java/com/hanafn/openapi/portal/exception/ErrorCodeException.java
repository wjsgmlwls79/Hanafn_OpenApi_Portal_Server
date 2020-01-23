package com.hanafn.openapi.portal.exception;

import lombok.Getter;

@Getter
public class ErrorCodeException extends RuntimeException {
    protected String errorCode = "";
    protected Object detailMessage;

    public ErrorCodeException(String message) {
        super(message);
    }

    public ErrorCodeException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCodeException(String errorCode, String message, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.detailMessage = details;
    }

    public ErrorCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
