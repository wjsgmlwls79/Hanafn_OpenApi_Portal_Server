package com.hanafn.openapi.portal.cmct.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class OAuthRequest {
    private String authorizedGrantTypes;
    private String scope;

    @Data
    public static class ClientInfo {
        private String appKey;
        @NotBlank
        private String clientId;
        @NotBlank
        private String clientSecret;
        @NotBlank
        private String entrCd;
        @NotBlank
        private String decryptedEnckey;
        @NotBlank
        private String hfnCd;
    }
}
