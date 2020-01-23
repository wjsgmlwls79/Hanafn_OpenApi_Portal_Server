package com.hanafn.openapi.portal.security.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class HfnLoginRequest {

    @NotBlank
    private String hfnCd;

    @NotBlank
    private String hfnId;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String siteCd;

    private String userKey;

    @Override
    public String toString() {
    	StringBuffer buffer = new StringBuffer();

        buffer.append("hfnCd=");
        buffer.append(hfnCd);
        buffer.append(", hfnId=");
        buffer.append(hfnId);
        buffer.append(", siteCd=");
        buffer.append(siteCd);
        buffer.append(", username=");
        buffer.append(username);
        buffer.append(", password=");
        buffer.append("****");

        return buffer.toString();
    }
}