package com.hanafn.openapi.portal.exception.handler;

import java.util.Date;

import lombok.Data;

@Data
public class ErrorDetails {
	private Date timestamp;
	private int status;
	private String error;
	private String message;
	private String path;
	
	public ErrorDetails(int status, String error, String message, String path) {
	    super();
	    this.status = status;
	    this.error = error;
	    this.message = message;
	    this.timestamp = new Date();
	  }
}
