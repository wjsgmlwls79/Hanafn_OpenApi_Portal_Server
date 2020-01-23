package com.hanafn.openapi.portal.security.dto;

import lombok.Data;

@Data
public class LoginResponse {
	private String accessToken;
    private String tokenType = "Bearer";
    private String userType;
    private String tmpPasswordYn;
    private String portalTosYn;
    private String privacyTosYn;
    private String pwdChangeDt;
    private String userKey;
    private boolean expire;

    public LoginResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
