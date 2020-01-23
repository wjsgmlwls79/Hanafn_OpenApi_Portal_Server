package com.hanafn.openapi.portal.security.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String siteCd;

    @Override
    public String toString() {
    	StringBuffer buffer = new StringBuffer();

        buffer.append("siteCd=");
        buffer.append(siteCd);
        buffer.append(", username=");
        buffer.append(username);
        buffer.append(", password=");
        buffer.append("****");

        return buffer.toString();
    }

    @Data
    public static class tokenRefesh {
        private String username;
        private String password;
        private String siteCd;
    }
}