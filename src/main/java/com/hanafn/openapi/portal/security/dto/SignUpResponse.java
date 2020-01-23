package com.hanafn.openapi.portal.security.dto;

import lombok.Data;

@Data
public class SignUpResponse {
	private Boolean success;
	private String message;

	public SignUpResponse(Boolean success, String message) {
		this.success = success;
		this.message = message;
	}
}
