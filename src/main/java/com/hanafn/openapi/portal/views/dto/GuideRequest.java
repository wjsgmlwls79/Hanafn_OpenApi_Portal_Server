package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

import java.util.Map;

@Data
public class GuideRequest {
    private String appKey;
    private String clientId;
    private String clientSecret;
    private String entrCd;
    private String uri;
    private String url;
    private String method;
    private String encKey;
    private String hfnCd;
    private String gwType;
    private Map<String, Object> params;
}
