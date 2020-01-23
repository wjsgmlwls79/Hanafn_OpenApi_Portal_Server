package com.hanafn.openapi.portal.security.dto;

import lombok.Data;

@Data
public class SSOLoginResponse {
    private String retCd;
    private String redirect;
    private String hfnCd;
    private String hfnId;
    private String username;
}
