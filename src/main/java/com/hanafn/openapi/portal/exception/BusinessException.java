package com.hanafn.openapi.portal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @implNote  비즈니스 로직 예외처리용 클래스(사용자에게 보여지는 용도로 사용)
 * (예시) ID가 중복입니다 / 비밀번호가 일치하지 않습니다. / 이메일이 중복입니다.
 */
@ResponseStatus(HttpStatus.ACCEPTED)
public class BusinessException extends ErrorCodeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String errorCode, String message){
        super(errorCode, message);
    }

    public BusinessException(String errorCode, String message, Object details){
        super(errorCode, message, details);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
