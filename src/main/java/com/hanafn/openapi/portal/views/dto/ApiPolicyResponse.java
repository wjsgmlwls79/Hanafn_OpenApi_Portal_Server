package com.hanafn.openapi.portal.views.dto;

import lombok.Data;

@Data
public class ApiPolicyResponse {
    private String apiId;
    private String apiNm;
    private String maxUser;
    private String maxSize;
    private String ctnt;
    private String gubun;
    private String regUser;
    private String regDttm;
    private String modUser;
    private String modDttm;
}
