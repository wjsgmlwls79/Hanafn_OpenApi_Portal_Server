package com.hanafn.openapi.portal.exception;

import lombok.Data;

@Data
public class ErrorResponse{
    private String message;
    private String errorCode;
    private Object details;

    public ErrorResponse(ErrorCodeException t) {
        this.message = t.getMessage();
        this.errorCode = t.getErrorCode();
        this.details = t.getDetailMessage();
    }
}
