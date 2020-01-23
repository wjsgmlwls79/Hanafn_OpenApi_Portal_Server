package com.hanafn.openapi.portal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends ErrorCodeException {
    public BadRequestException(String message) {
        super(message);
    }
    public BadRequestException(String errorCode, String message) {
        super(errorCode, message);
    }
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}