package com.hanafn.openapi.portal.exception.handler;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@RestController
@Slf4j
public class HandlerNotValidException extends ResponseEntityExceptionHandler {
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
																  HttpHeaders headers, HttpStatus status, WebRequest request) {

		HttpServletRequest sevletRequest = ((ServletWebRequest)request).getRequest();

		String errorArgument = "";
		String errorMsg = "";
		for (ObjectError error : ex.getBindingResult().getAllErrors()) {
			for (Object argumentObj : error.getArguments()){
				if(argumentObj instanceof DefaultMessageSourceResolvable) {
					DefaultMessageSourceResolvable data = (DefaultMessageSourceResolvable)argumentObj;
					errorArgument = data.getDefaultMessage();
				}
			}

			errorMsg = error.getDefaultMessage() + " [" + errorArgument + "]";
		}

		ErrorDetails errorDetails = new ErrorDetails(status.value(), status.name(),
				errorMsg, sevletRequest.getRequestURI());

		log.error("[" + errorDetails + "]");

		return new ResponseEntity<Object>(errorDetails, status);
	}
}
