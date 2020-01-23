package com.hanafn.openapi.portal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ServerException extends ErrorCodeException {
    public ServerException(String message) {
        super(message);
    }
    public ServerException(String errorCode, String message) {
        super(errorCode,message);
    }
    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }
}